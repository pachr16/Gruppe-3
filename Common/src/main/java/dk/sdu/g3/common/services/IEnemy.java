/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.sdu.g3.common.services;

/**
 *
 * @author robertfrancisti
 */
public interface IEnemy {
    
     boolean create(IUnit unit);
     int getCurrentWave();
     public  void createWave();
    
}
