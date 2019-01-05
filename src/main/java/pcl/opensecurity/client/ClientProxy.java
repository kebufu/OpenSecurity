package pcl.opensecurity.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pcl.opensecurity.Config;
import pcl.opensecurity.OpenSecurity;
import pcl.opensecurity.client.models.CamouflageBakedModel;
import pcl.opensecurity.client.models.ModColourManager;
import pcl.opensecurity.client.models.ModelBakeEventHandler;
import pcl.opensecurity.client.models.ModelNanoFogSwarm;
import pcl.opensecurity.client.renderer.*;
import pcl.opensecurity.client.sounds.AlarmResource;
import pcl.opensecurity.common.CommonProxy;
import pcl.opensecurity.common.ContentRegistry;
import pcl.opensecurity.common.Reference;
import pcl.opensecurity.common.entity.EntityEnergyBolt;
import pcl.opensecurity.common.entity.EntityNanoFogSwarm;
import pcl.opensecurity.common.items.ItemCard;
import pcl.opensecurity.common.nanofog.BakedModelLoader;
import pcl.opensecurity.common.tileentity.TileEntityEnergyTurret;
import pcl.opensecurity.common.tileentity.TileEntityKeypad;
import pcl.opensecurity.manual.ManualPathProvider;
import pcl.opensecurity.util.FileUtils;

import java.io.File;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent evt) {
        SecurityTerminalRender.showFoundTerminals(evt);
    }

    @SubscribeEvent
    public void colorHandlerEventBlock(ColorHandlerEvent.Block event) {
        ContentRegistry.nanoFog.initColorHandler(event.getBlockColors());
    }

    @Override
    public World getWorld(int dimId) {
        World world = Minecraft.getMinecraft().world;
        if (world.provider.getDimension() == dimId) {
            return world;
        }
        return null;
    }

    @Override
    public void preinit() {
        super.preinit();
        Config.clientPreInit();


        ModelNanoFogSwarm.setupResolution(Config.getConfig().getCategory("client").get("nanoFogSwarmResolution").getInt());


        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ModelBakeEventHandler.instance);



        ModelLoaderRegistry.registerLoader(new BakedModelLoader());

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityKeypad.class, new RenderKeypad());

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnergyTurret.class, new RenderEnergyTurret());
        TileEntityItemStackRenderer.instance = new EnergyTurretRenderHelper();

        RenderingRegistry.registerEntityRenderingHandler(EntityEnergyBolt.class, RenderEntityEnergyBolt::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityNanoFogSwarm.class, NanoFogSwarmRenderer.FACTORY);

        if(OpenSecurity.debug)
            OpenSecurity.logger.info("Registered renderers/models");
    }

    @Override
    public void init() {
        super.init();
        Minecraft mc = Minecraft.getMinecraft();
        mc.getItemColors().registerItemColorHandler(new CardColorHandler(ContentRegistry.itemRFIDCard), ContentRegistry.itemRFIDCard);
        mc.getItemColors().registerItemColorHandler(new CardColorHandler(ContentRegistry.itemMagCard), ContentRegistry.itemMagCard);
        ModColourManager.registerColourHandlers();
        ManualPathProvider.initialize();
    }

    @Override
    public void registerModels() {
        registerBlockItem(ContentRegistry.alarmBlock, 0, Reference.Names.BLOCK_ALARM);
        registerBlockItem(ContentRegistry.doorController, 0, Reference.Names.BLOCK_DOOR_CONTROLLER);
        registerBlockItem(ContentRegistry.securityTerminal, 0, Reference.Names.BLOCK_SECURITY_TERMINAL);
        registerBlockItem(ContentRegistry.biometricReaderBlock, 0, Reference.Names.BLOCK_BIOMETRIC_READER);
        registerBlockItem(ContentRegistry.dataBlock, 0, Reference.Names.BLOCK_DATA);
        registerBlockItem(ContentRegistry.cardWriter, 0, Reference.Names.BLOCK_CARD_WRITER);
        registerBlockItem(ContentRegistry.cardDock, 0, Reference.Names.BLOCK_CARDDOCK);
        registerBlockItem(ContentRegistry.magReader, 0, Reference.Names.BLOCK_MAG_READER);
        registerBlockItem(ContentRegistry.keypadBlock, 0, Reference.Names.BLOCK_KEYPAD);
        registerBlockItem(ContentRegistry.entityDetector, 0, Reference.Names.BLOCK_ENTITY_DETECTOR);
        registerBlockItem(ContentRegistry.energyTurret, 0, Reference.Names.BLOCK_ENERGY_TURRET);
        registerBlockItem(ContentRegistry.rfidReader, 0, Reference.Names.BLOCK_RFID_READER);
        registerBlockItem(ContentRegistry.nanoFogTerminal, 0, Reference.Names.BLOCK_NANOFOG_TERMINAL);

        // BlockNanoFog uses custom texture/model loader for shield blocks
        ContentRegistry.nanoFog.initModel();
        registerBlockItem(ContentRegistry.nanoFog, 0, Reference.Names.BLOCK_NANOFOG);

        registerItem(ContentRegistry.secureDoorItem, Reference.Names.BLOCK_SECURE_DOOR);
        registerItem(ContentRegistry.securePrivateDoorItem, Reference.Names.BLOCK_PRIVATE_SECURE_DOOR);
        registerItem(ContentRegistry.itemRFIDCard, Reference.Names.ITEM_RFID_CARD);
        registerItem(ContentRegistry.rfidReaderCardItem, Reference.Names.ITEM_RFID_READER_CARD);
        registerItem(ContentRegistry.itemMagCard, Reference.Names.ITEM_MAG_CARD);
        registerItem(ContentRegistry.damageUpgradeItem, Reference.Names.ITEM_DAMAGE_UPGRADE);
        registerItem(ContentRegistry.movementUpgradeItem, Reference.Names.ITEM_MOVEMENT_UPGRADE);
        registerItem(ContentRegistry.energyUpgradeItem, Reference.Names.ITEM_ENERGY_UPGRADE);
        registerItem(ContentRegistry.cooldownUpgradeItem, Reference.Names.ITEM_COOLDOWN_UPGRADE);
        registerItem(ContentRegistry.nanoDNAItem, Reference.Names.ITEM_NANODNA);

        StateMapperBase ignoreState = new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
                return CamouflageBakedModel.variantTag;
            }
        };

        ModelLoader.setCustomStateMapper(ContentRegistry.doorController, ignoreState);
        ModelLoader.setCustomStateMapper(ContentRegistry.secureDoor, new StateMap.Builder().ignore(BlockDoor.POWERED).build());
        ModelLoader.setCustomStateMapper(ContentRegistry.privateSecureDoor, new StateMap.Builder().ignore(BlockDoor.POWERED).build());
    }

    private void registerBlockItem(final Block block, int meta, final String blockName) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(block.getRegistryName().toString()));
        if(OpenSecurity.debug)
            OpenSecurity.logger.info("Registering " + blockName + " Item Renderer");
    }

    private void registerItem(final Item item, final String itemName) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(OpenSecurity.MODID + ":" + itemName));
        if(OpenSecurity.debug)
            OpenSecurity.logger.info("Registering " + itemName + " Item Renderer");
    }

    private void listFilesForPath(final File path) {
        AlarmResource r = new AlarmResource();
        int i = 1;

        for(File fileEntry : FileUtils.listFilesForPath(path.getPath()))
            r.addSoundReferenceMapping(i++, fileEntry.getName()); //add map soundlocation -> recordX

        r.registerAsResourceLocation(); //finalise IResourcePack
    }

    @Override
    public void registerSounds() {
        File[] listOfFiles;
        File alarmSounds = new File("./mods/OpenSecurity/assets/opensecurity/sounds/alarms");

        if (!alarmSounds.exists())
            return;

        for(File file : alarmSounds.listFiles())
            if (file.isFile())
                OpenSecurity.alarmList.add(file.getName());

        listFilesForPath(alarmSounds);
    }

    private static class CardColorHandler implements IItemColor {
        private final ItemCard card;

        private CardColorHandler(ItemCard card) {
            this.card = card;
        }

        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            // TODO Auto-generated method stub
            return tintIndex == 0 ? 0xFFFFFF : card.getColor(stack);
        }
    }

}