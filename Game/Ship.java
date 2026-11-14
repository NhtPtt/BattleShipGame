public class Ship {
    char shipType;
    int shipSize;
    int shipHealth;
    int shipX;
    int shipY;
    int shipDirection; // 0 for horizontal, 1 for vertical
    int shipHits;
    int shipSunk;
    
    public Ship(char type, int size, int x, int y, int direction) {
            this.shipType = type;
            this.shipSize = size;
            this.shipHealth = size;
            this.shipX = x;
            this.shipY = y;
            this.shipDirection = direction;
            this.shipHits = 0;
            this.shipSunk = 0;
    }

    public void hit() {
        this.shipHits++;
        this.shipHealth--;
        if (this.shipHealth <= 0) {
            this.shipSunk = 1;
        }
    }

    public boolean isSunk() {
        return this.shipSunk == 1;
    }

    public char getShipType() {
        return this.shipType;
    }

    public int getShipSize() {
        return this.shipSize;
    }

    public int getShipHealth() {
        return this.shipHealth;
    }

    public int getShipX() {
        return this.shipX;
    }

    public int getShipY() {
        return this.shipY;
    }

    public int getShipDirection() {
        return this.shipDirection;
    }

    public int getShipHits() {
        return this.shipHits;
    }

    public int getShipSunk() {
        return this.shipSunk;
    }

    public void setShipType(char type) {
        this.shipType = type;
    }

    public void setShipSize(int size) {
        this.shipSize = size;
    }

    public void setShipHealth(int health) {
        this.shipHealth = health;
    }

    public void setShipX(int x) {
        this.shipX = x;
    }

    public void setShipY(int y) {
        this.shipY = y;
    }

    public void setShipDirection(int direction) {
        this.shipDirection = direction;
    }
}
