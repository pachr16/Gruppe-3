package dk.sdu.g3.player;

import dk.sdu.g3.common.data.Coordinate;
import dk.sdu.g3.common.rendering.Graphic;
import dk.sdu.g3.common.rendering.IRenderable;
import dk.sdu.g3.common.rendering.IStage;
import dk.sdu.g3.common.serviceLoader.ServiceLoader;
import dk.sdu.g3.common.services.IMap;
import dk.sdu.g3.common.services.IPlaceableEntity;
import dk.sdu.g3.common.services.IPlayer;
import dk.sdu.g3.common.services.ITower;
import dk.sdu.g3.common.services.ITowerFactory;
import dk.sdu.g3.engine.util.render.Dictionary.Dictionary;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

@ServiceProviders(value = {
    @ServiceProvider(service = IPlayer.class)
    ,
    @ServiceProvider(service = IStage.class),})
public class Player implements IPlayer, IStage {

    // functionality attributes
    private int hp = 50;
    private int gold = 100;
    private List<IPlaceableEntity> entityList = new ArrayList();
    private ServiceLoader factoryLoader;
    private ServiceLoader mapLoader;
    private ITower reservedTower;
    private Dictionary dict;

    // rendering attributes
    private final float width = 0.25f;
    private final float height = 0.6f;
    private final float posX = 0.83f;
    private final float posY = 0.5f;
    private Graphic backgroundfile = Graphic.TOWERPICKERBACKGROUND;
    private TextTest text = null;

    // TowerPicker stuff
    private TowerOnTowerPicker t1, t2, t3;
    private Object t1id, t2id, t3id;
    private ITowerFactory tf1, tf2, tf3;

    public Player() {
        mapLoader = new ServiceLoader(IMap.class);
        factoryLoader = new ServiceLoader(ITowerFactory.class);

        updateTowerFactories();
    }

    private void updateTowerFactories() {
        dict = new Dictionary();

        t1 = null;
        t1id = new Object();
        tf1 = null;

        t2 = null;
        t2id = new Object();
        tf2 = null;

        t3 = null;
        t3id = new Object();
        tf3 = null;

        for (ITowerFactory towerFactory : (List<ITowerFactory>) factoryLoader.getServiceProviderList()) {
            insertTower(towerFactory);
        }
    }

    @Override
    public int getCurrentHp() {
        return hp;
    }

    @Override
    public int getCurrentGold() {
        return gold;
    }

    @Override
    public List<IPlaceableEntity> getEntities() {
        return entityList;
    }

    @Override
    public void remove(IPlaceableEntity entity) {
        for (IMap map : (List<IMap>) mapLoader.getServiceProviderList()) {
            map.removeEntity(entity);
            entityList.remove(entity);
        }
    }

    @Override
    public boolean decreaseHp(int damage) {
        hp -= damage;
        if (hp <= 0) {
            return false;
        }
        return true;
    }

    @Override
    public void reserveTower(ITower tower) {
        if (tower.getCost() > this.gold) {
            System.out.println("You don't have enough gold for that!");
        } else {
            reservedTower = tower;
        }
    }

    @Override
    public void placeReservedTower(Coordinate coor) {
        if (reservedTower != null) {
            reservedTower.setPosition(coor);
            for (IMap map : (List<IMap>) mapLoader.getServiceProviderList()) {
                if (map.addEntity(reservedTower)) {
                    this.gold -= reservedTower.getCost();
                    entityList.add(reservedTower);
                    reservedTower = null;
                } else {
                    System.out.println("Can't place tower there!");
                }
            }
        }

        updateTowerFactories();
    }

    @Override
    public List<IRenderable> getRenderables() {
        ArrayList<IRenderable> renderlist = new ArrayList<>();

        if (tf1 != null) {
            renderlist.add(t1);
        }
        if (tf2 != null) {
            renderlist.add(t2);
        }
        if (tf3 != null) {
            renderlist.add(t3);
        }
        if (text == null) {
            text = new TextTest(this, this);
        }
        renderlist.add(text);
        return renderlist;
    }

    @Override
    public float getPosScaleX() {
        return this.posX;
    }

    @Override
    public float getPosScaleY() {
        return this.posY;
    }

    @Override
    public float getWithScale() {
        return this.width;
    }

    @Override
    public float getHigthScale() {
        return this.height;
    }

    @Override
    public Graphic getBackgroundFile() {
        return this.backgroundfile;
    }

    public void insertTower(ITowerFactory towerf) {

        if (t1 == null) {

            tf1 = towerf;
            t1 = new TowerOnTowerPicker(this, (float) 0.8, t1id, tf1.getFile());
            dict.insert(t1.getPosScaleX() - t1.getHigthScale() / 2, t1.getPosScaleY() - t1.getWithScale() / 2, t1.getPosScaleX() + t1.getHigthScale() / 2, t1.getPosScaleY() + t1.getWithScale() / 2, t1id);

            return;
        }
        if (t2 == null) {

            tf2 = towerf;
            t2 = new TowerOnTowerPicker(this, (float) 0.6, t2id, tf2.getFile());
            dict.insert(t2.getPosScaleX() - t2.getHigthScale() / 2, t2.getPosScaleY() - t2.getWithScale() / 2, t2.getPosScaleX() + t2.getHigthScale() / 2, t2.getPosScaleY() + t2.getWithScale() / 2, t2id);

            return;
        }
        if (t3 == null) {

            tf3 = towerf;
            t3 = new TowerOnTowerPicker(this, (float) 0.4, t3id, tf3.getFile());
            dict.insert(t3.getPosScaleX() - t3.getHigthScale() / 2, t3.getPosScaleY() - t3.getWithScale() / 2, t3.getPosScaleX() + t3.getHigthScale() / 2, t3.getPosScaleY() + t3.getWithScale() / 2, t3id);

            System.out.println("Can only hold 3 towerfactories");
            return;
        }
    }

    @Override
    public Object handleInput(float XScale, float YScale) {
        Object resolved = null;

        try {
            resolved = dict.search(XScale, YScale);
            if (resolved.equals(t1id)) {
                if (tf1 != null) {
                    return tf1.getNewTower();
                }
            }
            if (resolved.equals(t2id)) {
                if (tf2 != null) {
                    return tf2.getNewTower();
                }
            }
            if (resolved.equals(t3id)) {
                if (tf3 != null) {
                    return tf3.getNewTower();
                }
            }

        } catch (NullPointerException e) {
            System.out.println("The clicked location didn't correspond to a tower");
        }

        return resolved;
    }

    @Override
    public void earnGold(int gold) {
        this.gold += gold;
    }

}
