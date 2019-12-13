package blocks;

public enum BlockFace {
    LEFT,RIGHT,TOP,BOTTOM,FRONT,BACK;
    public boolean isSide(){
        return this == LEFT || this == RIGHT || this == BACK ||this == FRONT;
    }
}
