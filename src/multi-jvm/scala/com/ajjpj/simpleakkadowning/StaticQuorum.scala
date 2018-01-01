package com.ajjpj.simpleakkadowning

import com.ajjpj.simpleakkadowning.util.{AbstractDowningSpec, SimpleDowningConfig}


object StaticQuorum {
  object NoRolesConfig extends SimpleDowningConfig("static-quorum", "quorum-size" -> "3")
  object RoleWithOldestConfig extends SimpleDowningConfig("static-quorum", "quorum-size" -> "2", "role" -> "with-oldest")
  object RoleWithoutOldestConfig extends SimpleDowningConfig("static-quorum", "quorum-size" -> "2", "role" -> "without-oldest")
}

class StaticQuorumKeepOldestMultiJvm0 extends AbstractDowningSpec(StaticQuorum.NoRolesConfig, 1,2,3)
class StaticQuorumKeepOldestMultiJvm1 extends AbstractDowningSpec(StaticQuorum.NoRolesConfig, 1,2,3)
class StaticQuorumKeepOldestMultiJvm2 extends AbstractDowningSpec(StaticQuorum.NoRolesConfig, 1,2,3)
class StaticQuorumKeepOldestMultiJvm3 extends AbstractDowningSpec(StaticQuorum.NoRolesConfig, 1,2,3)
class StaticQuorumKeepOldestMultiJvm4 extends AbstractDowningSpec(StaticQuorum.NoRolesConfig, 1,2,3)
class StaticQuorumKeepOldestMultiJvm5 extends AbstractDowningSpec(StaticQuorum.NoRolesConfig, 1,2,3)

class StaticQuorumLoseOldestMultiJvm0 extends AbstractDowningSpec(StaticQuorum.NoRolesConfig, 3,4,5)
class StaticQuorumLoseOldestMultiJvm1 extends AbstractDowningSpec(StaticQuorum.NoRolesConfig, 3,4,5)
class StaticQuorumLoseOldestMultiJvm2 extends AbstractDowningSpec(StaticQuorum.NoRolesConfig, 3,4,5)
class StaticQuorumLoseOldestMultiJvm3 extends AbstractDowningSpec(StaticQuorum.NoRolesConfig, 3,4,5)
class StaticQuorumLoseOldestMultiJvm4 extends AbstractDowningSpec(StaticQuorum.NoRolesConfig, 3,4,5)
class StaticQuorumLoseOldestMultiJvm5 extends AbstractDowningSpec(StaticQuorum.NoRolesConfig, 3,4,5)

class StaticQuorumRoleWithOldest1MultiJvm0 extends AbstractDowningSpec(StaticQuorum.RoleWithOldestConfig, 1,2)
class StaticQuorumRoleWithOldest1MultiJvm1 extends AbstractDowningSpec(StaticQuorum.RoleWithOldestConfig, 1,2)
class StaticQuorumRoleWithOldest1MultiJvm2 extends AbstractDowningSpec(StaticQuorum.RoleWithOldestConfig, 1,2)
class StaticQuorumRoleWithOldest1MultiJvm3 extends AbstractDowningSpec(StaticQuorum.RoleWithOldestConfig, 1,2)
class StaticQuorumRoleWithOldest1MultiJvm4 extends AbstractDowningSpec(StaticQuorum.RoleWithOldestConfig, 1,2)
class StaticQuorumRoleWithOldest1MultiJvm5 extends AbstractDowningSpec(StaticQuorum.RoleWithOldestConfig, 1,2)

class StaticQuorumRoleWithOldest2MultiJvm0 extends AbstractDowningSpec(StaticQuorum.RoleWithOldestConfig, 2,3)
class StaticQuorumRoleWithOldest2MultiJvm1 extends AbstractDowningSpec(StaticQuorum.RoleWithOldestConfig, 2,3)
class StaticQuorumRoleWithOldest2MultiJvm2 extends AbstractDowningSpec(StaticQuorum.RoleWithOldestConfig, 2,3)
class StaticQuorumRoleWithOldest2MultiJvm3 extends AbstractDowningSpec(StaticQuorum.RoleWithOldestConfig, 2,3)
class StaticQuorumRoleWithOldest2MultiJvm4 extends AbstractDowningSpec(StaticQuorum.RoleWithOldestConfig, 2,3)
class StaticQuorumRoleWithOldest2MultiJvm5 extends AbstractDowningSpec(StaticQuorum.RoleWithOldestConfig, 2,3)

class StaticQuorumRoleWithoutOldest1MultiJvm0 extends AbstractDowningSpec(StaticQuorum.RoleWithoutOldestConfig, 3,4)
class StaticQuorumRoleWithoutOldest1MultiJvm1 extends AbstractDowningSpec(StaticQuorum.RoleWithoutOldestConfig, 3,4)
class StaticQuorumRoleWithoutOldest1MultiJvm2 extends AbstractDowningSpec(StaticQuorum.RoleWithoutOldestConfig, 3,4)
class StaticQuorumRoleWithoutOldest1MultiJvm3 extends AbstractDowningSpec(StaticQuorum.RoleWithoutOldestConfig, 3,4)
class StaticQuorumRoleWithoutOldest1MultiJvm4 extends AbstractDowningSpec(StaticQuorum.RoleWithoutOldestConfig, 3,4)
class StaticQuorumRoleWithoutOldest1MultiJvm5 extends AbstractDowningSpec(StaticQuorum.RoleWithoutOldestConfig, 3,4)

class StaticQuorumRoleWithoutOldest2MultiJvm0 extends AbstractDowningSpec(StaticQuorum.RoleWithoutOldestConfig, 4,5)
class StaticQuorumRoleWithoutOldest2MultiJvm1 extends AbstractDowningSpec(StaticQuorum.RoleWithoutOldestConfig, 4,5)
class StaticQuorumRoleWithoutOldest2MultiJvm2 extends AbstractDowningSpec(StaticQuorum.RoleWithoutOldestConfig, 4,5)
class StaticQuorumRoleWithoutOldest2MultiJvm3 extends AbstractDowningSpec(StaticQuorum.RoleWithoutOldestConfig, 4,5)
class StaticQuorumRoleWithoutOldest2MultiJvm4 extends AbstractDowningSpec(StaticQuorum.RoleWithoutOldestConfig, 4,5)
class StaticQuorumRoleWithoutOldest2MultiJvm5 extends AbstractDowningSpec(StaticQuorum.RoleWithoutOldestConfig, 4,5)
