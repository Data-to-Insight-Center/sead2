package org.seadva.metadatagen.metagen;

import org.seadva.metadatagen.util.MetadataResponse;

public abstract class BaseMetadataGen  implements MetadataGen{

    public MetadataResponse generateMetadataResponse(String id) {
    	MetadataResponse mR = new MetadataResponse();
    	mR.setMetadata(generateMetadata(id));
    	return mR;
    }
    
    @Deprecated public abstract String generateMetadata(String id);

}