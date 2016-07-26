package org.seadva.metadatagen.metagen;

import org.seadva.metadatagen.util.MetadataResponse;

/**
 * Metadata Generator interface
 */
public interface MetadataGen {

    public MetadataResponse generateMetadataResponse(String id);
    
    @Deprecated public String generateMetadata(String id);

}
