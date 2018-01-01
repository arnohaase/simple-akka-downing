package com.ajjpj.simpleakkadowning

import com.ajjpj.simpleakkadowning.util.{AbstractDowningSpec, SimpleDowningConfig}


object KeepMajority {
  object NoRolesConfig extends SimpleDowningConfig("keep-majority")
  object RoleWithOldestConfig extends SimpleDowningConfig("keep-majority", "role" -> "with-oldest")
  object RoleWithoutOldestConfig extends SimpleDowningConfig("keep-majority", "role" -> "without-oldest")
}

class KeepMajorityKeepOldestMultiJvm0 extends AbstractDowningSpec(KeepMajority.NoRolesConfig, 1,2,3)
class KeepMajorityKeepOldestMultiJvm1 extends AbstractDowningSpec(KeepMajority.NoRolesConfig, 1,2,3)
class KeepMajorityKeepOldestMultiJvm2 extends AbstractDowningSpec(KeepMajority.NoRolesConfig, 1,2,3)
class KeepMajorityKeepOldestMultiJvm3 extends AbstractDowningSpec(KeepMajority.NoRolesConfig, 1,2,3)
class KeepMajorityKeepOldestMultiJvm4 extends AbstractDowningSpec(KeepMajority.NoRolesConfig, 1,2,3)
class KeepMajorityKeepOldestMultiJvm5 extends AbstractDowningSpec(KeepMajority.NoRolesConfig, 1,2,3)

class KeepMajorityLoseOldestMultiJvm0 extends AbstractDowningSpec(KeepMajority.NoRolesConfig, 3,4,5)
class KeepMajorityLoseOldestMultiJvm1 extends AbstractDowningSpec(KeepMajority.NoRolesConfig, 3,4,5)
class KeepMajorityLoseOldestMultiJvm2 extends AbstractDowningSpec(KeepMajority.NoRolesConfig, 3,4,5)
class KeepMajorityLoseOldestMultiJvm3 extends AbstractDowningSpec(KeepMajority.NoRolesConfig, 3,4,5)
class KeepMajorityLoseOldestMultiJvm4 extends AbstractDowningSpec(KeepMajority.NoRolesConfig, 3,4,5)
class KeepMajorityLoseOldestMultiJvm5 extends AbstractDowningSpec(KeepMajority.NoRolesConfig, 3,4,5)

class KeepMajorityRoleWithOldest1MultiJvm0 extends AbstractDowningSpec(KeepMajority.RoleWithOldestConfig, 1,2)
class KeepMajorityRoleWithOldest1MultiJvm1 extends AbstractDowningSpec(KeepMajority.RoleWithOldestConfig, 1,2)
class KeepMajorityRoleWithOldest1MultiJvm2 extends AbstractDowningSpec(KeepMajority.RoleWithOldestConfig, 1,2)
class KeepMajorityRoleWithOldest1MultiJvm3 extends AbstractDowningSpec(KeepMajority.RoleWithOldestConfig, 1,2)
class KeepMajorityRoleWithOldest1MultiJvm4 extends AbstractDowningSpec(KeepMajority.RoleWithOldestConfig, 1,2)
class KeepMajorityRoleWithOldest1MultiJvm5 extends AbstractDowningSpec(KeepMajority.RoleWithOldestConfig, 1,2)

class KeepMajorityRoleWithOldest2MultiJvm0 extends AbstractDowningSpec(KeepMajority.RoleWithOldestConfig, 2,3)
class KeepMajorityRoleWithOldest2MultiJvm1 extends AbstractDowningSpec(KeepMajority.RoleWithOldestConfig, 2,3)
class KeepMajorityRoleWithOldest2MultiJvm2 extends AbstractDowningSpec(KeepMajority.RoleWithOldestConfig, 2,3)
class KeepMajorityRoleWithOldest2MultiJvm3 extends AbstractDowningSpec(KeepMajority.RoleWithOldestConfig, 2,3)
class KeepMajorityRoleWithOldest2MultiJvm4 extends AbstractDowningSpec(KeepMajority.RoleWithOldestConfig, 2,3)
class KeepMajorityRoleWithOldest2MultiJvm5 extends AbstractDowningSpec(KeepMajority.RoleWithOldestConfig, 2,3)

class KeepMajorityRoleWithoutOldest1MultiJvm0 extends AbstractDowningSpec(KeepMajority.RoleWithoutOldestConfig, 3,4)
class KeepMajorityRoleWithoutOldest1MultiJvm1 extends AbstractDowningSpec(KeepMajority.RoleWithoutOldestConfig, 3,4)
class KeepMajorityRoleWithoutOldest1MultiJvm2 extends AbstractDowningSpec(KeepMajority.RoleWithoutOldestConfig, 3,4)
class KeepMajorityRoleWithoutOldest1MultiJvm3 extends AbstractDowningSpec(KeepMajority.RoleWithoutOldestConfig, 3,4)
class KeepMajorityRoleWithoutOldest1MultiJvm4 extends AbstractDowningSpec(KeepMajority.RoleWithoutOldestConfig, 3,4)
class KeepMajorityRoleWithoutOldest1MultiJvm5 extends AbstractDowningSpec(KeepMajority.RoleWithoutOldestConfig, 3,4)

class KeepMajorityRoleWithoutOldest2MultiJvm0 extends AbstractDowningSpec(KeepMajority.RoleWithoutOldestConfig, 4,5)
class KeepMajorityRoleWithoutOldest2MultiJvm1 extends AbstractDowningSpec(KeepMajority.RoleWithoutOldestConfig, 4,5)
class KeepMajorityRoleWithoutOldest2MultiJvm2 extends AbstractDowningSpec(KeepMajority.RoleWithoutOldestConfig, 4,5)
class KeepMajorityRoleWithoutOldest2MultiJvm3 extends AbstractDowningSpec(KeepMajority.RoleWithoutOldestConfig, 4,5)
class KeepMajorityRoleWithoutOldest2MultiJvm4 extends AbstractDowningSpec(KeepMajority.RoleWithoutOldestConfig, 4,5)
class KeepMajorityRoleWithoutOldest2MultiJvm5 extends AbstractDowningSpec(KeepMajority.RoleWithoutOldestConfig, 4,5)
