package com.stars.services.id;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.ServerManager;
import com.stars.util.uid.IdGenerator;

/**
 * Created by zhaowenshuo on 2016/7/22.
 */
public class IdServiceImpl implements IdService {

    private IdGenerator idGenerator;

    private RoleIdAllocator roleIdAllocator;
    private ToolIdAllocator toolIdAllocator;
    private FamilyIdAllocator familyIdAllocator;
    private FamilyRedPacketIdAllocator familyRedPacketIdAllocator;

    @Override
    public void init() throws Throwable {
        BootstrapConfig config = ServerManager.getServer().getConfig();
        idGenerator = new IdGenerator();
        idGenerator.register("roleId", new RoleIdAllocator(config.getServerId()));
        idGenerator.register("toolId", new ToolIdAllocator(config.getServerId()));
        idGenerator.register("familyId", new FamilyIdAllocator(config.getServerId()));
        idGenerator.register("familyRedPacketId", new FamilyRedPacketIdAllocator(config.getServerId()));
        idGenerator.init();

        roleIdAllocator = (RoleIdAllocator) idGenerator.get("roleId");
        toolIdAllocator = (ToolIdAllocator) idGenerator.get("toolId");
        familyIdAllocator = (FamilyIdAllocator) idGenerator.get("familyId");
        familyRedPacketIdAllocator = (FamilyRedPacketIdAllocator) idGenerator.get("familyRedPacketId");
    }

    public long newRoleId() {
        return roleIdAllocator.newId();
    }

    public long newToolId() {
        return toolIdAllocator.newId();
    }

    public long newFamilyId() {
        return familyIdAllocator.newId();
    }

    public long newFamilyRedPacketId() {
        return familyRedPacketIdAllocator.newId();
    }

}
