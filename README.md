# simple-akka-downing
This is a simple Akka downing provider that provides some of the Lightbend's commercial version's functionality 
(and aims at emulating its config API) but is open source.

## Introduction

### Network Partitions

Network partitions can cause problems for Akka cluster applications: While nodes are unreachable, no new 
nodes can fully join the cluster, no new leader can be elected etc. At the application level, cluster
shards (or singletons or whatever) running on an unreachable node are unavailable while the node is
unreachable.

If the network problem goes away fast enough, the cluster will heal itself and all is well. Applications
however should usually not wait indefinitely for this to happen, especially since the unreachability
may be due to a hard crash of one of the JVMs and can therefore be permanent.

### Downing provider

So the usual strategy is to define a timeout after which nodes are assumed to be permanently unreachable,
and removed from the cluster. The naive approach (which is the only strategy Akka supports out of the box)
is to `auto-down` unreachable nodes after a timeout, which can lead to 'split brain' with a cluster 
disintegrating into two smaller clusters, each of which assumes it is the only one. This is bad - for
details see 
[Akka Cluster documentation](https://doc.akka.io/docs/akka/current/cluster-usage.html?language=scala#auto-downing-do-not-use-).

Akka cluster however has a config value `akka.cluster.downing-provider-class`, which allows an application to 
provide an `akka.cluster.DowningProvider.DowningProvider` as a strategy for deciding which nodes to 'down'
when.

Lightbend provides a 
[Split Brain Resolver](https://developer.lightbend.com/docs/akka-commercial-addons/current/split-brain-resolver.html) 
that uses this hook as part of its commercial 
[Reactive Platform](http://lightbend.com/platform). The 
[documentation](https://developer.lightbend.com/docs/akka-commercial-addons/current/split-brain-resolver.html) 
gives an excellent explanation of the problem and possible strategies. 

## Usage

This library is intended as an open source alternative to Lightbend's commercial Split Brain Resolver, providing a
subset of its features with a configuration format that emulates the commercial library closely.

The first step using the library is set the downing provider in `application.conf`:

```
akka.cluster.downing-provider-class = com.ajjpj.simpleakkadowning.SimpleAkkaDowningProvider
```

### Configuration overview

The library has three top-level config parameters: `down-removal-margin`, `stable-after` and `active-strategy`.

```
simple-akka-downing {
  # Time margin after which shards or singletons that belonged to a downed/removed
  #  partition are created in surviving partition. The purpose of this margin is that
  #  in case of a network partition the persistent actors in the non-surviving partitions
  #  must be stopped before corresponding persistent actors are started somewhere else.
  #
  # Disable with "off" or specify a duration to enable.
  #
  # See akka.cluster.down-removal-margin
  down-removal-margin = 10s

  # Time margin after which unreachable nodes in a stable cluster state (i.e. no nodes changed
  #  their membership state or their reachability) are treated as permanently unreachable, and
  #  the split-brain resolution strategy kicks in.
  stable-after = 10s

  # The active strategy is one of static-quorum, keep-majority and keep-oldest. It is triggered
  #  after the cluster configuration has been stable for an interval of 'stable-after'.
  #
  # static-quorum defines a fixed number of nodes, and a network partition must have at least
  #  this number of reachable nodes (in a given role, if that is specified) in order to be allowed
  #  to survive. If the quorum size is picked bigger than half the maximum number of cluster nodes,
  #  this strategy is completely robust. It does not however work well with a dynamically growing
  #  (or shrinking) cluster.
  #
  # keep-majority uses the number of cluster nodes as the baseline and requires a network partition
  #  to have more than half that number of (reachable) nodes in order to be allowed to survive. This
  #  fully supports elastically growing and shrinking clusters, but there are rare race conditions
  #  that can lead to both partitions to be downed or - potentially worse - both partitions to survive.
  #
  # keep-oldest requires the oldest member to be part of a partition for it to survive. This can be
  #  useful since the oldest node is where cluster singletons are running, so this strategy does not
  #  singletons to be migrated and restarted. It reliably prevents split brain, but it can lead to
  #  a situation where 2 nodes survive and 25 nodes are downed. To deal with the pathological special
  #  case that the oldest node is in a network partition of its own, the flag 'down-if-alone' can be
  #  used to specify the oldest node if it is all by itself.
  active-strategy = keep-majority
  
  # ...
}
```

The first two configure the timing of detecting and resolving network partitions. For a comprehensive discussion,
see the 
[Commercial Split Brain Resolver documentation](https://developer.lightbend.com/docs/akka-commercial-addons/current/split-brain-resolver.html#expected-failover-time).
In essence, there are three phases: 
1. It takes the cluster some time - in the order of seconds - to notice that a node is unreachable, and then spread this
   knowledge across the cluster. This depends on things like the number of missed heart beats necessary for a node to
   be treated as unreachable. We do not want to be over sensitive here to be able to distinguish a minor glitch from 
   a real problem. This duration depends on several technical config parameters of the cluster and does not have any
   direct relation to this library and its configuration. It is however part of the time from a network problem first
   occurring and its resolution.
2. `stable-after` is the time that simple-akka-downing waits after a node becomes unreachable before assuming it is
   *permanently* unreachable. This timer is restarted whenever the cluster configuration changes, e.g. some node
   becomes reachable or unreachable. Nodes on different sides of a network partition have no way of communicating
   with each other and could therefore assume the partition to have started at slightly different points in time.
   This config property is a safety margin and should be set higher the bigger the cluster is. See the 
   [Commercial Split Brain Resolver documentation](https://developer.lightbend.com/docs/akka-commercial-addons/current/split-brain-resolver.html#expected-failover-time)
   for quantitative advice.   
3. `down-removal-margin` is an additional safety margin Akka uses for restarting Singletons and Shard Regions when
   they are terminated. 

The third parameter is `active-strategy`. It decides the strategy used for deciding which side of a partition should
be terminated and which should survive, and must be one of the following values: 

### static-quorum

The `static-quorum` strategy defines a minimum number of nodes a partition must consist of in order to be allowed to
survive. The `quorum-size` should always be larger than half the cluster size, in which case this is a simple and
reliable strategy. It is obviously best suited for fixed cluster sizes.

Specifying a `role` causes the `quorum-size` to apply to nodes with that role. Using this, `static-quorum` can be used
to make this strategy applicable to elastically growing and shrinking clusters by defining a core role of fixed size
which determines survivorship in case of a network partition, and elastically adding nodes without this role. 

```
simple-akka-downing {
  # ...

  active-strategy = static-quorum

  static-quorum {
    # minimum number of nodes that the cluster must have
    quorum-size = undefined

    # if the 'role' is defined the decision is based only on members with that 'role'
    role = ""
  }
}
```

### keep-majority

The `keep-majority` strategy requires more than half the `Up` nodes to be reachable in order for a partition to 
survive. This works well with elastically growing and shrinking clusters and requires no additional configuration. 

There is however a small margin of error: If the cluster configuration is changing when the partition occurs, the
partitions may have different ideas on which nodes are full members, which in turn can lead to both partitions being
downed or - usually worse - both remaining alive, causing a split brain. This is highly unlikely, but it can happen.

A `role` can be specified for `keep-majority`, in which case a majority of nodes in the given role is required for
survival.    

```
simple-akka-downing {
  # ...
  
  active-strategy = keep-majority

  keep-majority {
    role = ""
  }
}
```

### keep-oldest

The `keep-oldest` strategy requires the oldest node in the cluster to be reachable in order for a partition to
survive. This is more exotic than the previous strategies, but it can be advantageous because cluster singletons
run on the oldest node, so this strategy minimizes cluster singleton restart.

It can however result in e.g. 29 nodes being downed in a cluster of 31 nodes, with only two nodes surviving. That may
not be a bad thing - it is after all what this strategy exists for - but you should know what you are in for.

There is special treatment for the situation that the oldest node is the only one to become unreachable: If the
config parameter `down-if-alone` is set to `on` (which is the default), the single node will be downed and the rest
of the cluster survives. This allows the rest of the cluster to continue running if the oldest node crashes for some
reason.  
  
```
simple-akka-downing {
  # ...
  
  active-strategy = keep-oldest

  keep-oldest {
    down-if-alone = on
  }
}
```

## Build dependencies

The library is on Maven central. Its Maven coordinates are 

```
<dependency>
    <groupId>com.ajjpj.simple-akka-downing</groupId>
    <artifactId>simple-akka-downing_2.11</artifactId>
    <version>0.9.1</version>
</dependency>
```

(for Scala 2.11) or

```
<dependency>
    <groupId>com.ajjpj.simple-akka-downing</groupId>
    <artifactId>simple-akka-downing_2.12</artifactId>
    <version>0.9.1</version>
</dependency>
```

for Scala 2.12.

With sbt, use 

```
"com.ajjpj.simple-akka-downing" %% "simple-akka-downing" % "0.9.1" 
```


 

