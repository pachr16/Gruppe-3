package dk.sdu.g3.common.services;

import dk.sdu.g3.common.data.Coordinate;

public interface IPlaceableEntity {
    
    void setPosition(Coordinate coord);
    Coordinate getCurrentPosition();
    int getCost();
    int getFootprint();

}
