package Varg;

import battlecode.common.*;

public class Scout {
	
	RobotController rc;
	
	public void logic(RobotController RC) throws GameActionException {
		
		rc = RC;
		
		while (true) {
			try {
				
				Map.donate(rc);			
				Map.dodge(rc);
				Map.resetArchons(rc);

				MapLocation myLocation = rc.getLocation();					
				RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());			
				TreeInfo[] trees = RC.senseNearbyTrees(-1, Team.NEUTRAL);
				MapLocation[] archons = rc.getInitialArchonLocations(rc.getTeam().opponent());
				
				if (myLocation.distanceTo(archons[0]) < 5 && rc.readBroadcast(183) == 0) {
					rc.broadcast(183, 1);
				}
				
				// Scan for enemies
				if (enemies.length > 0) {
					Map.dodge(rc, myLocation.directionTo(enemies[0].location).opposite());
					for (RobotInfo enemy : enemies) {
						if (enemy.getType() == RobotType.GARDENER) {
							Direction dir = myLocation.directionTo(enemy.location);
							if (myLocation.distanceTo(enemy.location) > (float)2.5 && !rc.hasMoved()) {
								Map.tryMove(rc, dir);
							}	
							else {
								rc.fireSingleShot(dir);
							}
						}
					}
				}
				
				// Scan for trees
				if (trees.length > 0) {	
					
					for (TreeInfo tree : trees) {
						if (tree.containedBullets > 0) {
							if (rc.canShake(tree.ID)) {	
								rc.shake(tree.ID);
								break;
							}
							else {
								if (!rc.hasMoved()) {
									Map.tryMove(rc, myLocation.directionTo(tree.location));
								}
							}
						}
					}
					
					if (rc.readBroadcast(4) == 0) {
						if (rc.getRoundNum() < 100) {
							rc.broadcast(175, 1);
						}
						rc.broadcast(4, 5);		
					}
				}
				
				else {
					if (!rc.hasMoved() && rc.readBroadcast(183) == 1) {
						moveRandomly(myLocation);
					}
					else {
						Map.tryMove(rc, myLocation.directionTo(archons[0]));
					}
				}
				
				// I'm alive
				rc.broadcast(6, 1);
				
				Clock.yield();		
				
			} catch (Exception e) {
		        System.out.println("Scout Exception");
		        e.printStackTrace();
			}
		}
	}
	
	private void moveRandomly(MapLocation myLocation) throws GameActionException {
		MapLocation target = pickTarget();
		Map.tryMove(rc, myLocation.directionTo(target));
	}
	
    private MapLocation pickTarget() throws GameActionException {
        RobotType type = rc.getType();
        MapLocation target = new MapLocation(-1, -1);
        target = rc.getLocation().add(Map.randomDirection(), type.sensorRadius - 1f);       
        return target;
    }
}
