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
import java.util.Random;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import dk.sdu.g3.common.serviceLoader.ServiceLoader;
import java.util.ArrayList;
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
    IMap realMap;
    List<IMap> mapList;
    List<IPathfinding> pathlist;
    List<IUnit> Unitlist;
    List<IPlaceableEntity> EntityList;
    List<IPlaceableEntity> EntitiesOnMap;
    List<IUnitFactory> UnitFactoryList;
    List<Coordinate> TestList;
    int unitNumber;
    //valid tileSize = 2 * map.tilesize
    public Enemy() {
        
    }

    
    // put this method in IController, since both player/enemy uses it
    @Override
    public void putEntityOnMap(IPlaceableEntity unit,IMap map1){
        System.out.println("you have entered the method :)");
  
        try{
            System.out.println("this is the try thingy");
        mapList = (List<IMap>) new ServiceLoader(IMap.class).getServiceProviderList();

        for (IMap map : mapList){
            map.generateMap(600, 600);
            System.out.println("map length" + map.getLengthX());
             Coordinate startPosition = new Coordinate(map.getTileSize(),random.nextInt((map.getLengthY()/(2*map.getTileSize())+1)*2*map.getTileSize() + map.getTileSize()));
             Coordinate endPosition = new Coordinate( map.getLengthX() - map.getTileSize(), map.getLengthY()/2);
               unit.setPosition(startPosition);
               pathlist = (List<IPathfinding>) new ServiceLoader(IPathfinding.class).getServiceProviderList();

               for (IPathfinding IPath : pathlist){
                   System.out.println("unit Position"+ unit.getCurrentPosition().getX() + "end position" + endPosition.getX() + "autoGenerated start position" + startPosition.getX());
                    TestList = IPath.generatePath(map, startPosition,endPosition);
                    
                    System.out.println("this is a thing");
                    addPathToUnit(IPath.generatePath(map, startPosition,endPosition),(IUnit) unit);
                    System.out.println("they actually did it right");
                    }
               
                map.addEntity(unit);
                System.out.println("adding entity");
                EntitiesOnMap.add(unit);
                System.out.println("sucess");
        }   

        }
        catch(Exception e){ 
            e.getMessage();
            System.out.println("the pathfinding is broken, please help it");
        }
                
    }
    
    public void addPathToUnit(List<Coordinate> path, IUnit unit){
        System.out.println("check");
        unit.setPath(path);
        System.out.println("");
    }
    @Override
    public int getCurrentWave() {
        return currentWave;
        
    }

    @Override
    public void createWave() {
        counter = 0;
        unitNumber = random.nextInt(101);
        while(unitNumber>0){
            create();
        }

    }


    @Override
    public  IPlaceableEntity create() {
        for (IUnitFactory unitfactory : UnitFactoryList){
            IUnit createdUnit = unitfactory.getNewUnit();
            EntityList.add(createdUnit);
            return createdUnit;    
        }
        return null;
    }

    @Override
    public List<IPlaceableEntity> getEntities() {
        return EntityList;
    }

    @Override
    public void remove(IPlaceableEntity unit) {
                for (IMap map : mapList){
             map.removeEntity(unit);
             EntitiesOnMap.remove(unit);
        }
    }

    @Override
    public boolean decreaseHp(int damage) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public boolean Update(){
        for(IMap map : mapList){
        if(counter < EntityList.size()){
            putEntityOnMap(EntityList.get(counter),map.getMap());
            counter++;
        }
        }
        if(EntitiesOnMap.isEmpty()){
        return false;
        }
        return true;
    }
    

}