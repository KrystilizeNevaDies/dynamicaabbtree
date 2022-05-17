package org.krystilize.dynvisualizer;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.AABBf;
import pl.pateman.dynamicaabbtree.AABBTree;
import pl.pateman.dynamicaabbtree.AABBTreeNode;
import pl.pateman.dynamicaabbtree.Boundable;
import pl.pateman.dynamicaabbtree.Identifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DynamicAABBVisualiser {
    public static void main(String[] args) {
        AABBTree<MinestomEntity> tree = new AABBTree<>(0.00001f);

        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        // Create the instance
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        // Set the ChunkGenerator
        instanceContainer.setGenerator(unit ->
                unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));

        // Commands
        MinecraftServer.getCommandManager().register(new SummonCommand());


        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });

        globalEventHandler.addListener(EntitySpawnEvent.class, event -> {
            tree.add(new MinestomEntity(event.getEntity()));
        });
        globalEventHandler.addListener(EntityTickEvent.class, event -> {
            if (event.getEntity().getVelocity().lengthSquared() > 0) {
                tree.update(new MinestomEntity(event.getEntity()));
            }
        });
        globalEventHandler.addListener(InstanceTickEvent.class, event -> {
            Instance instance = event.getInstance();

            // Render the tree
            tree.getNodes().forEach(createRenderer(instance));

            if (instance.getWorldAge() % 20 == 0) {
                for (Player player : instance.getPlayers()) {
                    AABBf aabb = new AABBf();
                    MinestomEntity.applyTo(player, aabb);
                    List<MinestomEntity> overlaps = new ArrayList<>();
                    tree.detectOverlaps(aabb, overlaps);

                    for (MinestomEntity overlap : overlaps) {
                        if (overlap.entity == player) {
                            continue;
                        }
                        player.sendMessage("Overlap: " + overlap.entity().getEntityType() + " at " + overlap.entity().getPosition());
                    }
                }
            }
        });

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);
    }

    private static Consumer<AABBTreeNode<MinestomEntity>> createRenderer(Instance instance) {
        return node -> {
            if (instance.getWorldAge() % 20 != 0) {
                return;
            }
            if (node == null) {
                return;
            }
            AABBf aabb = node.getAABB();
            boolean leaf = node.isLeaf();

            double d = aabb.minX;

            while (true) {
                if (d > aabb.maxX) {
                    d = aabb.maxX;
                }
                particle(instance, d, aabb.minY, aabb.minZ, leaf);
                particle(instance, d, aabb.minY, aabb.maxZ, leaf);
                particle(instance, d, aabb.maxY, aabb.minZ, leaf);
                particle(instance, d, aabb.maxY, aabb.maxZ, leaf);
                if (d == aabb.maxX) {
                    break;
                }
                d += 0.5;
            }

            d = aabb.minY;
            while (true) {
                if (d > aabb.maxY) {
                    d = aabb.maxY;
                }
                particle(instance, aabb.minX, d, aabb.minZ, leaf);
                particle(instance, aabb.minX, d, aabb.maxZ, leaf);
                particle(instance, aabb.maxX, d, aabb.minZ, leaf);
                particle(instance, aabb.maxX, d, aabb.maxZ, leaf);
                if (d == aabb.maxY) {
                    break;
                }
                d += 0.5;
            }

            d = aabb.minZ;
            while (true) {
                if (d > aabb.maxZ) {
                    d = aabb.maxZ;
                }
                particle(instance, aabb.minX, aabb.minY, d, leaf);
                particle(instance, aabb.minX, aabb.maxY, d, leaf);
                particle(instance, aabb.maxX, aabb.minY, d, leaf);
                particle(instance, aabb.maxX, aabb.maxY, d, leaf);
                if (d == aabb.maxZ) {
                    break;
                }
                d += 0.5;
            }
        };
    }

    private static void particle(Instance instance, double x, double y, double z, boolean leaf) {
        ParticlePacket packet = ParticleCreator.createParticlePacket(leaf ? Particle.FLAME : Particle.SMOKE, x, y, z, 0, 0, 0, 1);
        PacketUtils.sendGroupedPacket(instance.getPlayers(), packet);
    }

    public record MinestomEntity(@NotNull Entity entity) implements Boundable, Identifiable {

        @Override
        public @NotNull AABBf getAABB(@NotNull AABBf dest) {
            applyTo(entity(), dest);
            return dest;
        }

        public static void applyTo(@NotNull Entity entity, @NotNull AABBf dest) {
            dest.minX = (float) (entity.getBoundingBox().minX() + entity.getPosition().x());
            dest.minY = (float) (entity.getBoundingBox().minY() + entity.getPosition().y());
            dest.minZ = (float) (entity.getBoundingBox().minZ() + entity.getPosition().z());
            dest.maxX = (float) (entity.getBoundingBox().maxX() + entity.getPosition().x());
            dest.maxY = (float) (entity.getBoundingBox().maxY() + entity.getPosition().y());
            dest.maxZ = (float) (entity.getBoundingBox().maxZ() + entity.getPosition().z());
        }

        @Override
        public long getID() {
            return entity.getEntityId();
        }
    }
}