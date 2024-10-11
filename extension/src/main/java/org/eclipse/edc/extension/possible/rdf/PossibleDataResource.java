package org.eclipse.edc.extension.possible.rdf;

public class PossibleDataResource {
    public PossibleDataResource(String assetId, String datasetId) {
        this.assetId = assetId;
        this.datasetId = datasetId;
    }

    String assetId;
    // datasetId for the catalog
    String datasetId;

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }
    public String getDatasetId() {
        return datasetId;
    }
    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }
}
