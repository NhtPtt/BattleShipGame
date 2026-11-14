public class Ship {
    // Define the possible directions for the ship
    public enum Direction {
        HORIZONTAL, VERTICAL
    }

    private char type;
    private int size;
    private int x; // col
    private int y; // row
    private Direction direction;
    private int hits;

    public Ship(char type, int size, int x, int y, Direction direction) {
        this.type = type;
        this.size = size;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.hits = 0;
    }

    public void hit() {
        if (!isSunk()) {
            this.hits++;
        }
    }

    public boolean isSunk() {
        return this.hits >= this.size;
    }

    // Check if the ship occupies a specific coordinate
    public boolean occupies(int targetX, int targetY) {
        // Check if the target coordinates are within the bounds of the ship
        if (this.direction == Direction.HORIZONTAL) {
            return targetY == this.y && targetX >= this.x && targetX < this.x + this.size;
        } else {
            return targetX == this.x && targetY >= this.y && targetY < this.y + this.size;
        }
    }

    // Getters and setters
    public char getType() { return type; }
    public int getSize() { return size; }
    public int getX() { return x; }
    public int getY() { return y; }
    public Direction getDirection() { return direction; }
    public int getHits() { return hits; }
    public int getHealth() { return size - hits; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}