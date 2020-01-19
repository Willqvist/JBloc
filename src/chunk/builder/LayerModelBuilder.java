package chunk.builder;

import engine.model.ModelBuilder;

public class LayerModelBuilder {
    private ModelBuilder opaqueModelBuilder = new ModelBuilder(50000);
    private ModelBuilder transparentModelBuilder = new ModelBuilder(50000);
    private boolean occupied = false;

    public ModelBuilder getOpaqueModelBuilder() {
        return opaqueModelBuilder;
    }

    public ModelBuilder getTransparentModelBuilder() {
        return transparentModelBuilder;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public LayerModelBuilder setOpaqueModelBuilder(ModelBuilder opaqueModelBuilder) {
        this.opaqueModelBuilder = opaqueModelBuilder;
        return this;
    }

    public LayerModelBuilder setTransparentModelBuilder(ModelBuilder transparentModelBuilder) {
        this.transparentModelBuilder = transparentModelBuilder;
        return this;
    }

    public LayerModelBuilder setOccupied(boolean occupied) {
        this.occupied = occupied;
        return this;
    }
}
