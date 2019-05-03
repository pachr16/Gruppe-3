/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.sdu.g3.enemy;

import dk.sdu.g3.common.data.Coordinate;
import dk.sdu.g3.common.entities.ILifeFunctions;
import dk.sdu.g3.common.services.IEnemy;
import dk.sdu.g3.common.services.IMap;
import dk.sdu.g3.common.services.IPathfinding;
import dk.sdu.g3.common.services.IPlaceableEntity;
import dk.sdu.g3.common.services.IUnit;
import dk.sdu.g3.common.services.IUnitFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import dk.sdu.g3.common.serviceLoader.ServiceLoader;
import java.util.List;

/**
 *
 * @author robertfrancisti
 */

@ServiceProviders(value = {
    @ServiceProvider(service = IEnemy.class)
})
public class Enemy implements IEnemy {
    int currentWave = 0;
    int gold = 0;
    Random random = new Random();
    int bigUnits;
    int smallUnits;
    int counter;
    ArrayList<IPlaceableEntity> EntityList = new ArrayList();
    ArrayList<IUnitFactory> UnitFactoryList = new ArrayList();
    serviceLoaderEnemy unitLoader = new serviceLoaderEnemy();
    //valid tileSize = 2 * map.tilesize
    public Enemy() {
        
    }

    
    // put this method in IController, since both player/enemy uses it
    public void putEntityOnMap(IUnit unit){
        for (IMap map : unitLoader.getSP(IMap.class)){
//             Coordinate startPosition = new Coordinate(map.getTileSize,random.nextInt(map.getLengthY()/(2*map.getTileSize))));
//               unit.setPosition(startPosition);
             map.addEntity(unit);
                

        }
    }
    
    public void removeEntityFromMap(IUnit unit){
        for (IMap map : unitLoader.getSP(IMap.class)){
             map.removeEntity(unit);
        }
    }
    public void addPathToUnit(Coordinate path, IUnit unit){
        unit.setPath(path);
    }
    @Override
    public int getCurrentWave() {
        return currentWave;
    }

    @Override
    public void createWave() {
        bigUnits = generateWaveComposition();
        smallUnits = 100 - bigUnits;
        while(bigUnits == counter){
            for (IUnitFactory unit : unitLoader.getSP(IUnitFactory.class)){
                EntityList.add(unit.getNewUnit());
                
                
            }
        }
        while(smallUnits == counter){
            for (IUnitFactory unit : unitLoader.getSP(IUnitFactory.class)){
                EntityList.add(unit.getNewUnit());
        
            }
        }
    }
    public int generateWaveComposition(){
        ArrayList<Object> wave = new ArrayList();
        int bigUnits = random.nextInt(51);
        
    return bigUnits;
}

    @Override
    public boolean create(IUnit unit) {
        for (IUnitFactory unit1 : unitLoader.getSP(IUnitFactory.class)){
            
                EntityList.add(unit1.getNewUnit(unit.getCurrentHp(),unit.getDamage() , unit.getFootprint(), unit.getCost(), unit.getAttackRange(), unit.getAttackSpeed(), unit.getCurrentPosition(), unit.getPath()));
            return true;    
        }
        return false;
    }

    @Override
    public List<IPlaceableEntity> getEntities() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remove(ILifeFunctions livingEntity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean decreaseHp(int damage) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    

    
    public class serviceLoaderEnemy {

        private final Lookup lookup = Lookup.getDefault();
        private Lookup.Result<IUnitFactory> result;

        public serviceLoaderEnemy() {
            //vars
            
            result = lookup.lookupResult(IUnitFactory.class); //Finds SP'
            result.addLookupListener(lookupListener);
            result.allItems();

            System.out.println("---IGamePluginService---");
            //inizial load
            for (IUnitFactory plugin : result.allInstances()) {
                System.out.println(plugin);
                UnitFactoryList.add(plugin);
            }
        }

        private final LookupListener lookupListener = new LookupListener() {
            @Override
            public void resultChanged(LookupEvent le) {

                Collection<? extends IUnitFactory> updated = result.allInstances();

                for (IUnitFactory us : updated) {
                    // Newly installed modules
                    if (!UnitFactoryList.contains(us)) {
                        UnitFactoryList.add(us);
                    }
                }

                // Stop and remove module
                for (IUnitFactory gs : UnitFactoryList) {
                    if (!updated.contains(gs)) {
                        
                        UnitFactoryList.remove(gs);
                    }
                }
            }
        };

        private <T> Collection<? extends T> getSP(Class<T> SPI) {
            return lookup.lookupAll(SPI);
        }

    }
    }
