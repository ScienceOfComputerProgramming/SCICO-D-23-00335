package eu.iv4xr.ux.pxtesting.study.PCG;

import eu.iv4xr.framework.extensions.occ.Event;

public class PCGEvent extends Event {
    
    public PCGEvent(String name) { super(name) ; }
    public int health;
    public int damage;
    public int score;
    public int time;
    public int totalenemies;
    public int leftenemies;
}
