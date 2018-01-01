package com.ajjpj.simpleakkadowning

import com.ajjpj.simpleakkadowning.util.{AbstractDowningSpec, SimpleDowningConfig}


object KeepOldest {
  object NoRolesConfig extends SimpleDowningConfig("keep-oldest", "down-if-alone" -> "off")
}

class KeepOldest1MultiJvm0 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1)
class KeepOldest1MultiJvm1 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1)
class KeepOldest1MultiJvm2 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1)
class KeepOldest1MultiJvm3 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1)
class KeepOldest1MultiJvm4 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1)
class KeepOldest1MultiJvm5 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1)

class KeepOldest2MultiJvm0 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1,2)
class KeepOldest2MultiJvm1 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1,2)
class KeepOldest2MultiJvm2 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1,2)
class KeepOldest2MultiJvm3 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1,2)
class KeepOldest2MultiJvm4 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1,2)
class KeepOldest2MultiJvm5 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1,2)

class KeepOldest3MultiJvm0 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1,5)
class KeepOldest3MultiJvm1 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1,5)
class KeepOldest3MultiJvm2 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1,5)
class KeepOldest3MultiJvm3 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1,5)
class KeepOldest3MultiJvm4 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1,5)
class KeepOldest3MultiJvm5 extends AbstractDowningSpec(KeepOldest.NoRolesConfig, 1,5)
