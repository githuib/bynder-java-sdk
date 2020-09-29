package com.bynder.sdk.query.upload;

import com.bynder.sdk.query.MetapropertyAttribute;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link UploadQuery} class methods.
 */
public class UploadQueryTest {

    public static final String EXPECTED_FILEPATH = "/path/to/file";
    public static final String EXPECTED_DEFAULT_FILENAME = "file";
    public static final String EXPECTED_FILENAME = "given_filename";
    public static final String EXPECTED_BRAND_ID = "brandId";
    public static final String EXPECTED_MEDIA_ID = "mediaId";
    public static final Boolean EXPECTED_AUDIT = Boolean.TRUE;
    public static final String EXPECTED_METAPROPERTY_1_ID = "metaproperty1Id";
    public static final String EXPECTED_OPTION_1_NAME = "option1Name";
    public static final String EXPECTED_METAPROPERTY_2_ID = "metaproperty2Id";
    public static final String EXPECTED_OPTION_2_NAME = "option2Name";
    public static final String EXPECTED_METAPROPERTY_3_ID = "metaproperty3Id";
    public static final String EXPECTED_OPTION_3_NAME = "option3Name";
    public static final List<MetapropertyAttribute> EXPECTED_METAPROPERTIES = new ArrayList<>();
    static {
        EXPECTED_METAPROPERTIES.add(new MetapropertyAttribute(
                EXPECTED_METAPROPERTY_1_ID,
                new String[]{EXPECTED_OPTION_1_NAME}
        ));
        EXPECTED_METAPROPERTIES.add(new MetapropertyAttribute(
                EXPECTED_METAPROPERTY_2_ID,
                new String[]{EXPECTED_OPTION_2_NAME}
        ));
        EXPECTED_METAPROPERTIES.add(new MetapropertyAttribute(
                EXPECTED_METAPROPERTY_3_ID,
                new String[]{EXPECTED_OPTION_3_NAME}
        ));
    }


    @Test
    public void initializeUploadQuery() {
        UploadQuery uploadQuery = new UploadQuery(EXPECTED_FILEPATH, EXPECTED_BRAND_ID)
                .setMediaId(EXPECTED_MEDIA_ID)
                .setAudit(EXPECTED_AUDIT)
                .addMetaproperty(EXPECTED_METAPROPERTY_1_ID, EXPECTED_OPTION_1_NAME)
                .addMetaproperty(EXPECTED_METAPROPERTY_2_ID, EXPECTED_OPTION_2_NAME)
                .addMetaproperty(EXPECTED_METAPROPERTY_3_ID, EXPECTED_OPTION_3_NAME);

        assertEquals(EXPECTED_FILEPATH, uploadQuery.getFilepath());
        assertEquals(EXPECTED_DEFAULT_FILENAME, uploadQuery.getFilename());
        assertEquals(EXPECTED_BRAND_ID, uploadQuery.getBrandId());
        assertEquals(EXPECTED_MEDIA_ID, uploadQuery.getMediaId());
        assertEquals(EXPECTED_AUDIT, uploadQuery.isAudit());
        assertEquals(EXPECTED_METAPROPERTIES, uploadQuery.getMetaproperties());
    }

    @Test
    public void initializeUploadQueryWithFilename() {
        UploadQuery uploadQuery = new UploadQuery(EXPECTED_FILEPATH, EXPECTED_FILENAME, EXPECTED_BRAND_ID)
                .setMediaId(EXPECTED_MEDIA_ID)
                .setAudit(EXPECTED_AUDIT)
                .addMetaproperty(EXPECTED_METAPROPERTY_1_ID, EXPECTED_OPTION_1_NAME)
                .addMetaproperty(EXPECTED_METAPROPERTY_2_ID, EXPECTED_OPTION_2_NAME)
                .addMetaproperty(EXPECTED_METAPROPERTY_3_ID, EXPECTED_OPTION_3_NAME);

        assertEquals(EXPECTED_FILEPATH, uploadQuery.getFilepath());
        assertEquals(EXPECTED_FILENAME, uploadQuery.getFilename());
        assertEquals(EXPECTED_BRAND_ID, uploadQuery.getBrandId());
        assertEquals(EXPECTED_MEDIA_ID, uploadQuery.getMediaId());
        assertEquals(EXPECTED_AUDIT, uploadQuery.isAudit());
        assertEquals(EXPECTED_METAPROPERTIES, uploadQuery.getMetaproperties());
    }

}
