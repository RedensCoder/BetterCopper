package org.pherment.bettercopper.Data;

import java.util.UUID;

public record CopperLocksData(UUID ownerUuid, UUID lockUuid, boolean locked) {
}
