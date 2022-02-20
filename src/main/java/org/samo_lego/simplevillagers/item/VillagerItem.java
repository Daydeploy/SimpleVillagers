package org.samo_lego.simplevillagers.item;

import eu.pb4.polymer.api.item.SimplePolymerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class VillagerItem extends SimplePolymerItem {

    public VillagerItem(Properties settings) {
        super(settings, Items.VILLAGER_SPAWN_EGG);
    }

    /*@Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player user, @NotNull InteractionHand hand) {
        final ItemStack item = user.getItemInHand(hand);
        System.out.println("VillagerItem.use " + item);
        if (!world.isClientSide()) {
            final Villager villager = new Villager(EntityType.VILLAGER, world);

            villager.load(item.getOrCreateTagElement("SimpleVillagers"));
            world.addFreshEntity(villager);

            return InteractionResultHolder.consume(item);
        }
        return InteractionResultHolder.pass(item);
    }*/

    @Override
    public InteractionResult useOn(UseOnContext context) {
        System.out.println("VillagerItem.useOnCTX");
        final Level level = context.getLevel();

        if (!(level instanceof ServerLevel world)) {
            return InteractionResult.SUCCESS;
        }

        final ItemStack handStack = context.getItemInHand();
        final BlockPos clickedPos = context.getClickedPos();
        final Direction direction = context.getClickedFace();
        final BlockState blockState = level.getBlockState(clickedPos);

        final BlockPos blockPos2 = blockState.getCollisionShape(level, clickedPos).isEmpty() ? clickedPos : clickedPos.relative(direction);

        final Entity villager = EntityType.VILLAGER.spawn(world, handStack, context.getPlayer(), blockPos2, MobSpawnType.SPAWN_EGG, true, !Objects.equals(clickedPos, blockPos2) && direction == Direction.UP);

        System.out.println("Entity:; " + villager);
        if (villager != null) {
            this.loadVillager(villager, handStack);
            handStack.shrink(1);
            level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, clickedPos);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        System.out.println("VillagerItem.useLNG");
        final ItemStack handStack = player.getItemInHand(usedHand);
        final BlockHitResult hitResult = SpawnEggItem.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

        if (!(level instanceof ServerLevel)) {
            return InteractionResultHolder.success(handStack);
        }

        final BlockPos blockPos = hitResult.getBlockPos();
        if (((HitResult) hitResult).getType() != HitResult.Type.BLOCK ||
            !(level.getBlockState(blockPos).getBlock() instanceof LiquidBlock)) {
            return InteractionResultHolder.pass(handStack);
        }

        if (!level.mayInteract(player, blockPos) || !player.mayUseItemAt(blockPos, hitResult.getDirection(), handStack)) {
            return InteractionResultHolder.fail(handStack);
        }

        final Entity villager = EntityType.VILLAGER.spawn((ServerLevel) level, handStack, player, blockPos, MobSpawnType.SPAWN_EGG, false, false);
        if (villager == null) {
            return InteractionResultHolder.pass(handStack);
        }

        this.loadVillager(villager, handStack);

        if (!player.getAbilities().instabuild) {
            handStack.shrink(1);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        level.gameEvent(GameEvent.ENTITY_PLACE, player);

        return InteractionResultHolder.consume(handStack);
    }

    private void loadVillager(Entity villager, ItemStack handStack) {
        System.out.println("VillagerItem.loadVillager " + handStack);
        final CompoundTag tag = handStack.getOrCreateTag();
        tag.put("Pos", this.newDoubleList(villager.getX(), villager.getY(), villager.getZ()));

        System.out.println(handStack.hashCode() + " Tag: " + tag);
        villager.load(tag);
    }

    private ListTag newDoubleList(double ... numbers) {
        ListTag listTag = new ListTag();
        for (double d : numbers) {
            listTag.add(DoubleTag.valueOf(d));
        }
        return listTag;
    }
}