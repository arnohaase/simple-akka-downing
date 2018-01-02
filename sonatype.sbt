sonatypeProfileName := "arnohaase"
publishMavenStyle := true

licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage := Some(url("https://github.com/arnohaase/simple-akka-downing"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/arnohaase/simple-akka-downing"), "scm:git@github.com:arnohaase/simple-akka-downing.git"
  )
)
developers := List(
  Developer(id="arnohaase", name="Arno Haase", email="arno.haase@haase-consulting.com", url=url("http://haase-consulting.com"))
)