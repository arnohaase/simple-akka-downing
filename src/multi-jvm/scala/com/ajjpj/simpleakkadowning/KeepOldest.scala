package com.ajjpj.simpleakkadowning

import com.ajjpj.simpleakkadowning.util.{AbstractDowningSpec, SimpleDowningConfig}


object KeepOldest {
  object DefaultConfig extends SimpleDowningConfig("keep-oldest")
  object NoDownIfAloneConfig extends SimpleDowningConfig("keep-oldest", "down-if-alone" -> "off")
}

class KeepOldest1MultiJvm0 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 1,2)
class KeepOldest1MultiJvm1 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 1,2)
class KeepOldest1MultiJvm2 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 1,2)
class KeepOldest1MultiJvm3 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 1,2)
class KeepOldest1MultiJvm4 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 1,2)
class KeepOldest1MultiJvm5 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 1,2)

class KeepOldest2MultiJvm0 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 1,5)
class KeepOldest2MultiJvm1 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 1,5)
class KeepOldest2MultiJvm2 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 1,5)
class KeepOldest2MultiJvm3 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 1,5)
class KeepOldest2MultiJvm4 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 1,5)
class KeepOldest2MultiJvm5 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 1,5)

class KeepOldestDownIfAloneMultiJvm0 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 2,3,4,5)
class KeepOldestDownIfAloneMultiJvm1 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 2,3,4,5)
class KeepOldestDownIfAloneMultiJvm2 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 2,3,4,5)
class KeepOldestDownIfAloneMultiJvm3 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 2,3,4,5)
class KeepOldestDownIfAloneMultiJvm4 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 2,3,4,5)
class KeepOldestDownIfAloneMultiJvm5 extends AbstractDowningSpec(KeepOldest.DefaultConfig, 2,3,4,5)

class KeepOldestNoDownIfAloneMultiJvm0 extends AbstractDowningSpec(KeepOldest.NoDownIfAloneConfig, 1)
class KeepOldestNoDownIfAloneMultiJvm1 extends AbstractDowningSpec(KeepOldest.NoDownIfAloneConfig, 1)
class KeepOldestNoDownIfAloneMultiJvm2 extends AbstractDowningSpec(KeepOldest.NoDownIfAloneConfig, 1)
class KeepOldestNoDownIfAloneMultiJvm3 extends AbstractDowningSpec(KeepOldest.NoDownIfAloneConfig, 1)
class KeepOldestNoDownIfAloneMultiJvm4 extends AbstractDowningSpec(KeepOldest.NoDownIfAloneConfig, 1)
class KeepOldestNoDownIfAloneMultiJvm5 extends AbstractDowningSpec(KeepOldest.NoDownIfAloneConfig, 1)

