/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
/**
 * <p>
 * This package provides classes for converting raw data sources into a form which can be used by standard WorldWind
 * components, such as {@link gov.nasa.worldwind.layers.Layer} and {@link gov.nasa.worldwind.globes.ElevationModel}. The
 * gov.nasa.worldwind.data package contains two key interfaces: DataRaster, and DataStoreProducer. Older versions of the
 * gov.nasa.worldwind.data package included DataDescriptor, which has been removed from WorldWind. This section
 * describes the role of each interface. A guide to updating code which uses DataDescriptor can be found
 * <a href="#Section_DataDescriptorPortingGuide">here</a>.
 *
 * <p>
 * {@link gov.nasa.worldwind.data.DataRaster} encapsulates the dimensions, geographic
 * {@link gov.nasa.worldwind.geom.Sector}, and data of a two-dimensional raster grid. DataRaster provides an interface
 * to draw one geographic raster into another, according to each raster's geographic bounds. There are three concrete
 * implementations of DataRaster:
 * <ul>
 * <li>{@link gov.nasa.worldwind.data.BufferedImageRaster} - uses a {@link java.awt.image.BufferedImage} as a source of
 * geographically referenced image data.</li>
 * <li>{@link gov.nasa.worldwind.data.BufferWrapperRaster} - uses a {@link gov.nasa.worldwind.util.BufferWrapper} as a
 * source of geographically referenced scalar data. BufferWrapperRaster typically represents integer or floating point
 * elevation data.</li>
 * <li>{@link gov.nasa.worldwind.data.CachedDataRaster} - a wrapper implementation of DataRaster which lazily loads a
 * DataRaster from a specified source. The lazily loaded DataRaster is kept in a
 * {@link gov.nasa.worldwind.cache.MemoryCache}, which can be shared across multiple CachedDataRasters. CachedDataRaster
 * is useful when an application needs to operate on a number of raster data sources which do not fit into main
 * memory.</li>
 * </ul>
 * Additionally, there are two interfaces for performing I/O operations on DataRaster:
 * <ul>
 * <li>{@link gov.nasa.worldwind.data.DataRasterReader} - implementations can read image formats accepted by
 * {@link javax.imageio.ImageIO}, GeoTIFF images, GeoTIFF elevations, Raster Product Format (RPF) imagery, and Band
 * Interleaved by Line (BIL) formatted data.</li>
 * <li>{@link gov.nasa.worldwind.data.DataRasterWriter} - implementations can write image formats accepted by ImageIO,
 * GeoTIFF images, GeoTIFF elevations, compressed Direct Draw Surface (DDS) images, and Band Interleaved by Line (BIL)
 * formatted data.</li>
 * </ul>
 *
 * <p>
 * {@link gov.nasa.worldwind.data.DataStoreProducer} provides a common interface for converting raw data sources into a
 * form which can be used by standard WorldWind components. There are three concrete implementations of
 * DataStoreProducer:
 * <ul>
 * <li>{@link gov.nasa.worldwind.data.TiledElevationProducer} - converts georeferenced image files into the WorldWind
 * tile cache structure, which can be rendered on the globe by a {@link gov.nasa.worldwind.layers.TiledImageLayer}.
 * </li>
 * <li>{@link gov.nasa.worldwind.data.TiledImageProducer} - converts georeferenced elevation data files into the World
 * Wind tile cache structure, which can become part of the globe's surface terrain by using a
 * {@link gov.nasa.worldwind.terrain.BasicElevationModel}.</li>
 * <li>{@link gov.nasa.worldwind.data.WWDotNetLayerSetConverter} - converts data in the WorldWind .NET tile cache
 * structure into the WorldWind Java tile cache structure.</li>
 * </ul>
 *
 * <p>
 * <strong>Data Configuration Documents</strong> are a common mechanism and file format for describing a WorldWind
 * component's configuration. While data configuration documents are not part of the gov.nasa.worldwind.data package,
 * they are used as a configuration exchange mechanism by the classes in gov.nasa.worldwind.data. For example,
 * DataStoreProducer returns a DOM Document describing the data it produces. Understanding how to use data configuration
 * documents is important to leveraging the functionality of the gov.nasa.worldwind.data package. The section
 * <a href="#Section_UseCaseExamples">Common Use Case Examples</a> provides examples of how to use data configuration
 * documents to manage the data produced by classes in this package.
 *
 * <p>
 * <strong>DataDescriptor</strong> defined an interface for representing meta information about WorldWind cache data. It
 * has been replaced with data configuration documents, which provide a common mechanism to describe a component's
 * configuration. For information how to update code which uses DataDescriptor, see the
 * <a href="#Section_DataDescriptorPortingGuide">DataDescriptor Porting Guide</a>.
 *
 * <!--**************************************************************-->
 * <!--********************  Supported Formats  *********************-->
 * <!--**************************************************************-->
 *
 * <h2>Supported Formats</h2>
 * <ul>
 * <li>ADF - A proprietary BINARY format developed by ESRI for ARC/INFO GRID rasters stored in workspaces (INFO
 * directory, *.adf)
 * </li>
 * <li>ADRG - ARC Digitized Raster Graphics format; requires General Information File and Image file (*.gen + *.img)
 * </li>
 * <li>AirSAR - AirSAR Polarimetric (POLSAR) data format; images in P,L,C-wavelengths only (*.dat OR *_[p|l|c].dat)
 * </li>
 * <li>ASC - A non-proprietary ASCII format developed by ESRI for ARC/INFO rasters in ASCII format (*.asc, *.grd)</li>
 * <li>BAS - Fuji BAS Scanner Image (*.bas)</li>
 * <li>BIL - Band Interleaved by Line format of satellite data rasters, must be .HDR labeled (*.bil)</li>
 * <li>BIP - Band Interleaved by Pixel format of satellite data rasters, must be .HDR labeled (*.bip)</li>
 * <li>BLX - Magellan designed format for storing topographic data in Magellan GPS units (*.blx)</li>
 * <li>BMP - Microsoft Windows Device Independent Bitmap for raster graphics images, color and monochrome (*.bmp)</li>
 * <li>CADRG - Compressed ARC Digitized Raster Graphics format for digital maps and chart images, RPF and NITFS
 * compliant
 * </li>
 * <li>CIB - Controlled Image Base format for ortho-photos (rectified grayscale aerial images), RPF and NITFS compliant
 * </li>
 * <li>COASP - Defence R&amp;D Canada (DRDC) designed data raster format for Configurable Airborne SAR Processor
 * (*.coasp)
 * </li>
 * <li>COSAR - "COmplex SAR", Annotated Binary Matrix (TerraSAR-X), plain binary image raster, limited to 4GB size
 * (*.cosar)
 * </li>
 * <li>DEM - USGS designed format for storing a raster-based Digital Elevation Model (*.dem)</li>
 * <li>DOQ - USGS designed Digital Ortho Quad format for projections and transformations (*.doq)</li>
 * <li>DTED - Digital Terrain Elevation Data format for regularly spaced grid of elevation points (*.dt0, *.dt1, *.dt2)
 * </li>
 * <li>ENVI - ENVI raster types of 1bit, 2bit, 4bit, or 8bit, Int16, Float32 , limited support for projections and
 * datums(*.nvi + *.hdr)
 * </li>
 * <li>ESRI - ESRI raw raster data (aka ESRI BIL format) types of 8bit,16bit, and 32bit integers (*.bil + *.hdr)</li>
 * <li>GFF - Ground-based SAR Applications Testbed File Format (.gff)</li>
 * <li>GIF - CompuServe designed Graphics Interchange Format, up to 8bits per pixel, LWZ compressed (*.gif)</li>
 * <li>GRC - Northwood/Vertical Mapper Classified Grid Format (*.grc , *.tab)</li>
 * <li>GRD - Golden Software ASCII Grid (GSAG), Binary Grid (GSBG) and Golden Software 7 Binary Grid (GS7BG) (*.grd)
 * </li>
 * <li>GRD - Northwood/Vertical Mapper Numeric Grid Format (*.grd , *.tab)</li>
 * <li>GXF - GeoSoft Grid Exchange Format is an ASCII grid format for storing elevation data (*.gxf)</li>
 * <li>HGT - height format file for unprocessed global SRTM elevation data of 1x1 degree tiles (*.hgt or *.hgt.zip)
 * </li>
 * <li>ILWIS - ILWIS Raster Maps (*.mpr) and MapLists (*.mpl); projection info stored in the .CSY, georeference info in
 * the .GRF
 * </li>
 * <li>ISIS - USGS Astrogeology ISIS file format, versions 2 and 3 (*.isis)</li>
 * <li>JDEM - Japanese Digital Elevation Model for 32bit floating point elevation raster (*.mem)</li>
 * <li>JP2 - Joint Photographic Experts Group format based on wavelet compression (*.jp2, *.jp2k)</li>
 * <li>JPEG - Joint Photographic Experts Group format for images, uses lossy Digital Cosine Transform (DCT) compression
 * (*.jpg, *.jpeg)
 * </li>
 * <li>LCP - FARSITE v.4 Landscape multi-band raster format for terrain and tree canopy data; Int16 only; no projection
 * info, mostly UTM (.lcp)
 * </li>
 * <li>MFF - Vexcel MFF Raster (*.mff) and MFF2 (HKV) Raster (*.hdr)</li>
 * <li>MrSID - LizardTech's Multi-resolution Seamless Image Database file format based on wavelet compression (*.sid)
 * </li>
 * <li>NAT - Meteosat Second Generation (MSG) Native Archive Format, supports up to 12 bands, 10bits per band (.nat)
 * </li>
 * <li>NITF - National Imagery Transmission Format for the exchange, storage, and transmission of digital documents
 * (*.ntf, *.nitf)
 * </li>
 * <li>PCI - A database file format developed by PCI Geomatics for EASI/PACE remote sensing software (uncompressed .pix
 * and *.pcidsk)
 * </li>
 * <li>PGM - Netpbm grayscale image format (Portable Graymap) (*.pgm)</li>
 * <li>PNG - Portable Network Graphics format for grayscale, palette-based, and RGB-only images, lossless data
 * compression, supports transparency (*.png)
 * </li>
 * <li>PPM - Netpbm color image format (Portable Pixmap) (*.ppm)</li>
 * <li>RDB - R Raster Data (*.asc, *.rdb)</li>
 * <li>RIK - Swedish Grid RIK format for maps issued by the Swedish organization Lantm&auml;teriet (*.rik)</li>
 * <li>RPF - Raster Product Formats for digital images (CIB), maps and charts (CADRG), NITFS compliant (A.TOC)</li>
 * <li>SDAT - SAGA (System for Automated Geoscientific Analyses) GIS Binary Grid (binary dataset in *.sdat, ASCII header
 * in *.sgrd)
 * </li>
 * <li>TER - Leveller heightfields file format to store a one band of Float32 elevation values (*.ter)</li>
 * <li>TER - Terragen terrain file format to store a one band of Int16 elevation values (*.ter, *.terrain)</li>
 * <li>TIFF - Tagged Image File Format (TIFF) and GeoTIFF (*.tif, *.tiff, *.gtif)</li>
 *
 * <li>AirSAR, AirSAR Polarimetric Image, AirSAR</li>
 * <li>BT, VTP .bt (Binary Terrain) 1.3 Format, BT</li>
 * <li>CEOS, CEOS Image, CEOS</li>
 * <li>COASP, DRDC COASP SAR Processor Raster, COASP</li>
 * <li>COSAR, COSAR Annotated Binary Matrix (TerraSAR-X), COSAR</li>
 * <li>CPG, Convair PolGASP, CPG</li>
 * <li>DIMAP, SPOT DIMAP, DIMAP</li>
 * <li>DIPEx, DIPEx, DIPEx</li>
 * <li>EHdr, ESRI .hdr Labelled, EHdr</li>
 * <li>EIR, Erdas Imagine Raw, EIR</li>
 * <li>ELAS, ELAS, ELAS</li>
 * <li>ENVI, ENVI .hdr Labelled, ENVI</li>
 * <li>ERS, ERMapper .ers Labelled, ERS</li>
 * <li>ESAT, Envisat Image Format, ESAT</li>
 * <li>FAST, EOSAT FAST Format, FAST</li>
 * <li>FIT, FIT Image, FIT</li>
 * <li>FujiBAS, Fuji BAS Scanner Image, FujiBAS</li>
 * <li>GenBin, Generic Binary (.hdr Labelled), GenBin</li>
 * <li>GSC, GSC Geogrid, GSC</li>
 *
 * <li>HFA, Erdas Imagine Images (.img)A</li>
 * <li>IDA, Image Data and Analysis, IDA</li>
 * <li>ILWIS, ILWIS Raster Map, ILWIS</li>
 * <li>INGR, Intergraph Raster, INGR</li>
 * <li>JAXAPALSAR, JAXA PALSAR Product Reader (Level 1.1/1.5), JAXAPALSAR</li>
 * <li>L1B, NOAA Polar Orbiter Level 1b Data Set, L1B</li>
 * <li>LAN, Erdas .LAN/.GIS, LAN</li>
 * <li>LCP, FARSITE v.4 Landscape File (.lcp), LCP</li>
 * <li>MEM, In Memory Raster, MEM</li>
 * <li>MSGN, EUMETSAT Archive native (.nat), MSGN</li>
 * <li>NDF, NLAPS Data Format, NDF</li>
 * <li>PAux, PCI .aux Labelled, PAux</li>
 * <li>PCIDSK, PCIDSK Database File, PCIDSK</li>
 * <li>PDS, NASA Planetary Data System, PDS</li>
 *
 * <li>RMF, Raster Matrix Format, RMF</li>
 * <li>RS2, RadarSat 2 XML Product, RS2</li>
 * <li>RST, Idrisi Raster A.1, RST</li>
 * <li>SAGA, SAGA GIS Binary Grid (.sdat), SAGA</li>
 * <li>SAR_CEOS, CEOS SAR Image, SAR_CEOS</li>
 * <li>SDTS, SDTS Raster, SDTS</li>
 * <li>SGI, SGI Image File Format 1.0, SGI</li>
 * <li>SRP, Standard Raster Product (ASRP/USRP), SRP</li>
 * <li>TIL, EarthWatch .TIL, TIL</li>
 * <li>TSX, TerraSAR-X Product, TSX</li>
 * <li>XPM, X11 PixMap Format, XPM</li>
 *
 * </ul>
 *
 * <!--**************************************************************-->
 * <!--********************  Supported Projections  *****************-->
 * <!--**************************************************************-->
 *
 * <h2>Supported Projections</h2>
 * <ul>
 * <li>Albers Equal-Area Conic</li>
 * <li>Azimuthal Equidistant</li>
 * <li>Cassini-Soldner</li>
 * <li>Cylindrical Equal Area</li>
 * <li>Eckert IV</li>
 * <li>Eckert VI</li>
 * <li>Equidistant Conic</li>
 * <li>Equidistant Cylindrical</li>
 * <li>Equirectangular</li>
 * <li>Gauss-Kruger</li>
 * <li>Gall Stereographic</li>
 * <li>GEOS - Geostationary Satellite View
 * <li>Gnomonic</li>
 * <li>Hotine Oblique Mercator</li>
 * <li>Krovak</li>
 * <li>Laborde Oblique Mercator</li>
 * <li>Lambert Azimuthal Equal Area</li>
 * <li>Lambert Conic Conformal (1SP)</li>
 * <li>Lambert Conic Conformal (2SP)</li>
 * <li>Lambert Conic Conformal (2SP Belgium)</li>
 * <li>Lambert Cylindrical Equal Area</li>
 * <li>Mercator (1SP)</li>
 * <li>Mercator (2SP)</li>
 * <li>Miller Cylindrical</li>
 * <li>Mollweide</li>
 * <li>New Zealand Map Grid</li>
 * <li>Oblique Mercator</li>
 * <li>Oblique Stereographic</li>
 * <li>Orthographic</li>
 * <li>Polar Stereographic</li>
 * <li>Polyconic</li>
 * <li>Robinson</li>
 * <li>Rosenmund Oblique Mercator</li>
 * <li>Sinusoidal</li>
 * <li>Swiss Oblique Cylindrical</li>
 * <li>Swiss Oblique Mercator</li>
 * <li>Stereographic</li>
 * <li>Transverse Mercator</li>
 * <li>Transverse Mercator (Modified Alaska)</li>
 * <li>Transverse Mercator (South Oriented)</li>
 * <li>Tunisia Mining Grid</li>
 * <li>VanDerGrinten</li>
 * </ul>
 *
 * <!--**************************************************************-->
 * <!--********************  Deploying GDAL Libraries  **************-->
 * <!--**************************************************************-->
 *
 * <h2>Deploying WorldWind's GDAL Libraries</h2>
 *
 * The open-source GDAL and PROJ4 libraries are used to import many of WorldWind's supported data formats. WorldWind
 * uses GDAL version 1.7.2 and PROJ4 version ?.? along with LizardTech's Decode SDK version 7.0.0.2167 for MrSID
 * support.
 * <!--TODO: fill in PROJ4 version number above-->
 *
 * <h3>Supported Platforms</h3>
 *
 * GDAL and PROJ4 have been incorporated for MacOSX (Snow Leopard, 64-bit), Windows 32 and Windows 64. Support for Linux
 * 32-bit and 64-bit, and Solaris are expected in the very near future. If the GDAL library cannot be found, data import
 * operates without it but supports only a limited set of formats and projections, in particular, GeoTIFF, JPEG, PNG,
 * BIL and DTED, and either EPSG:4326f (WGS84, latitude/longitude), or UTM.
 *
 * <h3>GDAL Library Locations</h3>
 *
 * To simplify deployment, GDAL + PRO4 + MrSID bundles are provided as a single dynamic library with all dependent
 * libraries included. There is one such library per platform, each located in
 * <code>lib-external/gdal/<em>platform</em></code>, as follows:
 * <ul>
 * <li>Windows 32bit library <code>gdalalljni.dll</code> is located in <code>lib-external/gdal/win32/</code></li>
 * <li>Windows 64bit library <code>gdalalljni.dll</code> is located in <code>lib-external/gdal/win64/</code></li>
 * <li>Mac OSX library <code>libgdalalljni.jnilib</code> is in <code>lib-external/gdal/macosx/</code></li>
 * </ul>
 * The GDAL and PROJ4 libraries require data tables located in <code>lib-external/gdal/data</code>. WorldWind attempts
 * to locate GDAL libraries during startup. By default WorldWind will first look in the locations listed above, then in
 * the current path, and if no GDAL bundle was found, will try to locate the GDAL bundle in the sub-folders.
 * <!--TODO: which sub-folders?-->
 *
 * <h3>Deploying with Java Web Start</h3>
 *
 * Instructions for using the WorldWind GDAL libraries with a Java Web Start application are available at
 * <a href="https://goworldwind.org/getting-started/" target="_blank">https://goworldwind.org/getting-started/</a>.
 *
 * <!--**************************************************************-->
 * <!--********************  Use Case Examples  *********************-->
 * <!--**************************************************************-->
 *
 * <h2><a id="Section_UseCaseExamples">Common Use Case Examples</a></h2>
 *
 * The following examples demonstrate the most common use cases which the classes in gov.nasa.worldwind.data are
 * designed to address. Additionally, several examples demonstrate data management use cases using data configuration
 * documents. These examples constitute an overview of how to convert raw data sources into a form which can be consumed
 * by WorldWind components, then manage the data in its converted form.
 *
 * <!-- Example 1 -->
 * <p>
 * <strong><a id="Example_1">Example 1: Converting Georeferenced Imagery to the WorldWind Tile Structure</a>
 * </strong>
 * <blockquote>
 * <pre>
 * <code>
 * // Given a source image, and a path to a folder in the local file system which receives the image tiles, configure a
 * // TiledImageProducer to create a pyramid of images tiles in the WorldWind Java cache format.
 * String imagePath = ...;
 * String tiledImagePath = ...;
 * String tiledImageDisplayName = ...;
 *
 * // Create a parameter list which defines where the image is imported, and the name associated with it.
 * AVList params = new AVListImpl();
 * params.setValue(AVKey.FILE_STORE_LOCATION, WWIO.getParentFilePath(tiledImagePath));
 * params.setValue(AVKey.DATA_CACHE_NAME, WWIO.getFilename(tiledImagePath));
 * params.setValue(AVKey.DATASET_NAME, tiledImageDisplayName);
 *
 * // Create a TiledImageProducer to transform the source image to a pyramid of images tiles in the WorldWind
 * // Java cache format.
 * DataStoreProducer producer = new TiledImageProducer();
 * try
 * {
 * // Configure the TiledImageProducer with the parameter list and the image source.
 * producer.setStoreParameters(params);
 * producer.offerDataSource(new File(imagePath), null);
 * // Import the source image into the FileStore by converting it to the WorldWind Java cache format. This throws
 * // an exception if production fails for any reason.
 * producer.startProduction();
 * }
 * catch (Exception e)
 * {
 * // Exception attempting to create the image tiles. Revert any change made during production.
 * producer.removeProductionState();
 * }
 *
 * // Extract the data configuration document from the production results. If production sucessfully completed, the
 * // TiledImageProducer's production results contain a Document describing the converted imagery as a data
 * // configuration document.
 * Iterable&lt;?&gt; results = producer.getProductionResults();
 * if (results == null || results.iterator() == null || !results.iterator().hasNext())
 * return;
 *
 * Object o = results.iterator().next();
 * if (o == null || !(o instanceof Document))
 * return;
 *
 * Document dataConfigDoc = (Document) o;
 * </code>
 * </pre>
 * </blockquote>
 *
 * <!-- Example 2 -->
 * <p>
 * <strong><a id="Example_2">Example 2: Converting Georeferenced Elevation Data to the WorldWind Tile Structure</a>
 * </strong>
 * <blockquote>
 * Converting georeferenced elevation data can be accomplished by referring to
 * <a href="#Example_1">Example 1: Converting Georeferenced Imagery to the WorldWind Tile Structure</a>, and replacing
 * {@link gov.nasa.worldwind.data.TiledImageProducer} with {@link gov.nasa.worldwind.data.TiledElevationProducer}.
 * </blockquote>
 *
 * <!-- Example 3 -->
 * <p>
 * <strong><a id="Example_3">Example 3: Converting WorldWind .NET LayerSets to the WorldWind Java Tile Structure
 * </a></strong>
 * <blockquote>
 * Converting WorldWind .NET LayerSets can be accomplished by referring to
 * <a href="#Example_1">Example 1: Converting Georeferenced Imagery to the WorldWind Tile Structure</a>, and replacing
 * {@link gov.nasa.worldwind.data.TiledImageProducer} with {@link gov.nasa.worldwind.data.WWDotNetLayerSetConverter}.
 * </blockquote>
 *
 * <!-- Example 4 -->
 * <p>
 * <strong><a id="Example_4">Example 4: Reading Data Configuration Documents from the File System</a></strong>
 * <blockquote>
 * <pre>
 * <code>
 * // Given a string path to a data configuration file in the local file system, read the file as a DOM document, which
 * // can be consumed by WorldWind's Layer and ElevationModel factories. This code is backward compatible with
 * // DataDescriptor files. The method DataConfigurationUtils.convertToStandardDataConfigDocument automatically detects
 * // and transforms DataDescriptor documents into standard Layer and ElevationModel configuration documents.
 * String dataConfigPath = ...;
 * Document dataConfigDoc = WWXML.openDocument(dataConfigPath);
 * dataConfigDoc = DataConfigurationUtils.convertToStandardDataConfigDocument(dataConfigDoc);
 * </code>
 * </pre>
 * </blockquote>
 *
 * <!-- Example 5 -->
 * <p>
 * <strong><a id="Example_5">Example 5: Reading Data Configuration Documents from the WorldWind FileStore</a>
 * </strong>
 * <blockquote>
 * <pre>
 * <code>
 * // Given a path to a data configuration file in the WorldWind FileStore, read the file as a DOM document, which can
 * // be consumed by WorldWind's Layer and ElevationModel factories. This code is backward compatible with
 * // DataDescriptor files. The method DataConfigurationUtils.convertToStandardDataConfigDocument automatically detects
 * // and transforms DataDescriptor documents into standard Layer or ElevationModel configuration documents.
 * FileStore fileStore = ...;
 * String dataConfigPath = ...;
 *
 * // Look for the data configuration file in the local file cache, but not in the class path.
 * URL url = fileStore.findFile(dataConfigPath, false);
 * if (url == null)
 * {
 * // The specified path name does not exist in the file store.
 * return;
 * }
 *
 * Document dataConfigDoc = WWXML.openDocument(url);
 * dataConfigDoc = DataConfigurationUtils.convertToStandardDataConfigDocument(dataConfigDoc);
 * </code>
 * </pre>
 * </blockquote>
 *
 * <!-- Example 6 -->
 * <p>
 * <strong><a id="Example_6">Example 6: Writing Data Configuration Documents</a></strong>
 * <blockquote>
 * <pre>
 * <code>
 * // Given a path to the data configuration file's destination in the local file system, and a parameter list
 * // describing the data, create a standard data configuration document and write it to the file system. This code is
 * // not forward compatible with applications still using DataDescriptor. Code which uses
 * // FileStore.findDataDescriptors to find data configuration files, or DataDesccriptorReader to read data
 * // configuration files will not be able to find or read the file produced by this example.
 * String dataConfigPath = ...;
 * AVList params = ...;
 *
 * // Create a data configuration document from the specified parameters.
 * Document dataConfigDoc;
 * if (isTiledImageryData)
 * {
 * // If you're writing a data configuration file for tiled imagery, use the following:
 * dataConfigDoc = LayerConfiguration.createTiledImageLayerDocument(params);
 * }
 * else if (isTiledElevationData)
 * {
 * // If you're writing a data configuration file for tiled elevations, use the following:
 * dataConfigDoc = ElevationModelConfiguration.createBasicElevationModelDocument(params);
 * }
 * else
 * {
 * // Otherwise, you'll need to create your own document. WorldWind currently provides support for creating data
 * // configuration files for tiled imagery and tiled elevations. Write custom code to create a data configuration
 * // document which corresponds with your data. Use LayerConfiguration and ElevationModelConfiguration as
 * // references, and use the methods in WWXML to construct your document in memory.
 * }
 *
 * // Write the data configuration document to the file system.
 * WWXML.saveDocumentToFile(dataConfigDoc, dataConfigPath);
 * </code>
 * </pre>
 * </blockquote>
 *
 * <!-- Example 7 -->
 * <p>
 * <strong><a id="Example_7">Example 7: Searching for Data Configuration Documents in the File System</a></strong>
 * <blockquote>
 * <pre>
 * <code>
 * // Given a search path in the local file system to look for data configuration files, return a list of file paths
 * // representing the matches closest to the root: data configuration files who's ancestor directories contain another
 * // data configuration file are ignored. The search path cannot be null. This code is backward compatible with
 * // DataDescriptor files. The class DataConfigurationFilter accepts standard Layer and ElevationModel configuration
 * // files, DataDescriptor files, and WorldWind .NET LayerSet files.
 * String searchPath = ...;
 * String[] filePaths = WWIO.listDescendantFilenames(searchPath, new DataConfigurationFilter());
 *
 * if (filePaths == null || filePaths.length == 0)
 * {
 * // No data configuration files found in the file system.
 * return;
 * }
 * </code>
 * </pre>
 * </blockquote>
 *
 * <!-- Example 8 -->
 * <p>
 * <strong><a id="Example_8">Example 8: Searching for Data Configuration Documents in the WorldWind FileStore</a>
 * </strong>
 * <blockquote>
 * There are two methods of searching for data configuration files in the WorldWind FileStore. The first method
 * individually searches each FileStore location using the method
 * {@link gov.nasa.worldwind.util.WWIO#listDescendantFilenames(java.io.File, java.io.FileFilter, boolean)}. This method
 * is equivalent to calling the method <code>FileStore.findDataDescriptors(String)</code> (which has been removed), and
 * should be used by applications porting from DataDescriptor. Use this method when your application needs to control
 * the FileStore locations it searches for data configuration files.
 * <pre>
 * <code>
 * // Given a WorldWind FileStore location in which to look for data configuration files, return a list of cache names
 * // representing the matches closest to the root: data configuration files who's ancestor directories contain another
 * // data configuration file are ignored. This code is backward compatible with DataDescriptor files. The class
 * // DataConfigurationFilter accepts standard Layer and ElevationModel configuration files, DataDescriptor files, and
 * // WorldWind .NET LayerSet files.
 * String fileStoreLocation = ...;
 * String[] cacheNames = WWIO.listDescendantFilenames(fileStoreLocation, new DataConfigurationFilter(), false);
 *
 * if (cacheNames == null || cacheNames.length == 0)
 * {
 * // No data configuration files found in the FileStore.
 * return;
 * }
 * </code>
 * </pre> The second method searches the entire WorldWind FileStore under a relative file store path. Use this method
 * when your application doesn't care which FileStore location it searches for data configuration files, but may care
 * what relative file store path it searches under.
 * <pre>
 * <code>
 * // Given a search path in the WorldWind FileStore to look for data configuration files, return a list of cache
 * // names representing the matches closest to the root: data configuration files who's ancestor directories contain
 * // another data configuration file are ignored. If the search path is null, the method FileStore.listTopFileNames
 * // searches the entire FileStore, excluding the class path. This code is backward compatible with DataDescriptor
 * // files. The class DataConfigurationFilter accepts standard Layer and ElevationModel configuration files,
 * // DataDescriptor files, and WorldWind .NET LayerSet files.
 * FileStore fileStore = ...;
 * String fileStoreSearchPath = ...;
 * String[] cacheNames = fileStore.listTopFileNames(fileStoreSearchPath, new DataConfigurationFilter());
 *
 * if (cacheNames == null || cacheNames.length == 0)
 * {
 * // No data configuration files found in the FileStore.
 * return;
 * }
 * </code>
 * </pre>
 * </blockquote>
 *
 * <!-- Example 9 -->
 * <p>
 * <strong><a id="Example_9">Example 9: Creating WorldWind Components from Data Configuration Documents</a></strong>
 * <blockquote>
 * <pre>
 * <code>
 * // Given a data configuration document which describes tiled imagery or tiled elevations in the WorldWind
 * // FileStore, create a WorldWind Layer or ElevationModel according to the contents of the data configuration
 * // document. This code is backward compatible with DataDescriptor files if the data configuration file was opened as
 * // shown in <a href="#Example_5">Example 5</a> or <a href="#Example_8">Example 8</a>.
 * Document dataConfigDoc = ...;
 * AVList params = ...;
 * String filename = ...; // The data configuration's filename, relative to a WorldWind file store.
 *
 * // If the data configuration doesn't define a cache name, then compute one using the file's path relative to its
 * // file store directory.
 * String s = dataConfig.getString("DataCacheName");
 * if (s == null || s.length() == 0)
 * DataConfigurationUtils.getDataConfigCacheName(filename, params);
 *
 * String type = DataConfigurationUtils.getDataConfigType(domElement);
 * if (type != null &amp;&amp; type.equalsIgnoreCase("Layer"))
 * {
 * Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
 * Layer layer = (Layer) factory.createFromConfigSource(dataConfig, params);
 * }
 * else if (type != null &amp;&amp; type.equalsIgnoreCase("ElevationModel"))
 * {
 * // If the data configuration doesn't define the data's extreme elevations, provide default values using the
 * // minimum and maximum elevations of Earth.
 * if (dataConfig.getDouble("ExtremeElevations/@min") == null)
 * params.setValue(AVKey.ELEVATION_MIN, Earth.ELEVATION_MIN);
 * if (dataConfig.getDouble("ExtremeElevations/@max") == null)
 * params.setValue(AVKey.ELEVATION_MAX, Earth.ELEVATION_MAX); // Height of Mt. Everest.
 * Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.ELEVATION_MODEL_FACTORY);
 * ElevationModel elevationModel = (ElevationModel) factory.createFromConfigSource(dataConfig, params);
 * }
 * else
 * {
 * // Currently, WorldWind supports factory construction of Layers and ElevationModels from data configuration
 * // documents. If your data configuration document describes another type of component, you'll need to write your
 * // own construction routine. Use BasicLayerFactory and BasicElevationModelFactory as references.
 * return;
 * }
 * </code>
 * </pre>
 * </blockquote>
 *
 * <!--**************************************************************-->
 * <!--********************  DataDescriptor Porting Guide  **********-->
 * <!--**************************************************************-->
 *
 * <h2><a id="Section_DataDescriptorPortingGuide">DataDescriptor Porting Guide</a></h2>
 * DataDescriptor has been replaced with data configuration documents. This guide explains why DataDescriptor has been
 * replaced, provides information on backward compatability with data configuration, and outlines how to update code
 * which uses DataDescriptor.
 *
 * <p>
 * <strong>What Happened to DataDescriptor?</strong>
 * <br>
 * Older versions of the gov.nasa.worldwind.data package included the DataDescriptor interface, along with its
 * associated DataDescriptorReader and DataDescriptorWriter. DataDescriptor defined an interface and an XML file format
 * for representing meta information about WorldWind cache data. The XML files were called "data descriptor" files, and
 * were typically named "dataDescriptor.xml". Applications used these files to discover processed data in the World Wind
 * file store, create an in-memory DataDescriptor from the file, then create either a Layer or an ElevationModel from
 * the DataDescriptor. WorldWind needed a common mechanism to describe a component's configuration, but DataDescriptor
 * had two problems which prevented its use as a common mechanism: (1) it presented all information as a flat list of
 * key-value pairs, making it difficult to represent heirarchical information, and (2) it decoded complex properties
 * (for example lists, angles, colors) at read time based on the property name, making it impossible to decode complex
 * objects with unknown names. For these reasons DataDescriptor was replaced with data configuration documents. Data
 * configuration documents provide a standard XML document structure to describe a component's configuration. They
 * support heirarchical data, and enable data to be decoded after read time by any component which consumes the data
 * configuration document.
 *
 * <p>
 * <strong>Backward Compatibility with Data Configuration Documents</strong>
 * <br>
 * Data configuration documents have supporting utilities which are designed to read and transform DataDescriptor files
 * written by older versions of WorldWind. Applications which port usage of DataDescriptor to data configuration
 * documents can maintain backwards compatibility with the DataDescriptor files written by older versions of World Wind.
 * However, there is no mechanism for forward compatibililty with data configuration documents. Applications which still
 * use DataDescriptor files will not be able to recognize or read data configuration files.
 *
 * <p>
 * The section <a href="#Section_UseCaseExamples">Common Use Case Examples</a> provides usage examples of data
 * configuration which are backward compatible with DataDescriptor. <a href="#Example_4">Example 4</a> and
 * <a href="#Example_5">Example 5</a> demonstrate how to read both data configuration files and DataDescriptor files.
 * <a href="#Example_7">Example 7</a> and <a href="#Example_8">Example 8</a> demonstrate how to search for data
 * configuration files and DataDescriptor files in the file system or the WorldWind FileStore.
 * <a href="#Example_9">Example 9</a> demonstrates how to create a WorldWind Layer or ElevationModel from a data
 * configuration file, in a way which is backward compatible with DataDescriptor files.
 * <p>
 * The data configuration files created in <a href="#Example_1">Example 1</a>, <a href="#Example_2">Example 2</a>, and
 * <a href="#Example_3">Example 3</a> are not forward compatible with DataDescriptor. Likewise, neither are the data
 * configuration files written in <a href="#Example_6">Example 6</a>. Applications which still use DataDescriptor will
 * not be able to recognize or read these files.
 *
 * <p>
 * <strong>Updating Usage of DataDescriptor</strong>
 * <br>
 * Data configuration documents are designed to replace DataDescriptor as a mechanism for communicating configuration
 * metadata about WorldWind components. WorldWind provides utilities to read and write data configuration files, search
 * for data configuration files in the local file system or WorldWind file store, and create WorldWind components from a
 * data configuration document. The following section outlines how to replace usage of DataDescriptor with data
 * configuration files.
 * <ul>
 * <li><strong>Reading DataDescriptor Files from the File System</strong>
 * <br>
 * The following code snippet is an example of how DataDescriptorReader was used to open a DataDescriptor file from the
 * local file system. <a href="#Example_4">Example 4</a> and <a href="#Example_5">Example 5</a> show how to replace this
 * with usage of data configuration documents.
 * <blockquote>
 * <pre>
 * <code>
 * // Given a string path to a DataDescriptor file in the local file system, read the file as a DataDescriptor.
 * String dataDescriptorPath = ...;
 * DataDescriptorReader reader = new BasicDataDescriptorReader();
 * reader.setSource(new File(dataDescriptorPath));
 *
 * if (!reader.canRead())
 * {
 * // File path does not point to a DataDescriptor file.
 * return;
 * }
 *
 * DataDescriptor dataDescriptor = reader.read();
 * </code>
 * </pre>
 * </blockquote>
 * </li>
 * <li><strong>Writing DataDescriptor Files</strong>
 * <br>
 * The following code snippet is an example of how DataDescriptorWriter was used to save a DataDescriptor to the local
 * file system. <a href="#Example_6">Example 6</a> shows how to replace this with usage of data configuration documents.
 * <blockquote>
 * <pre>
 * <code>
 * // Given a path to the DataDescriptor file's destination in the local file system, and a parameter list
 * // describing the data, create a DataDescriptor and write it to the file system.
 * String dataDescriptorPath = ...;
 * AVList params = ...;
 * DataDescriptor descriptor = new BasicDataDescriptor();
 *
 * Object o = params.getValue(AVKey.FILE_STORE_LOCATION);
 * if (o != null)
 * descriptor.setFileStoreLocation(new java.io.File(o.toString()));
 *
 * o = params.getValue(AVKey.DATA_CACHE_NAME);
 * if (o != null)
 * descriptor.setFileStorePath(o.toString());
 *
 * o = params.getValue(AVKey.DATASET_NAME);
 * if (o != null)
 * descriptor.setName(o.toString());
 *
 * o = params.getValue(AVKey.DATA_TYPE);
 * if (o != null)
 * descriptor.setType(o.toString());
 *
 * for (java.util.Map.Entry&lt;String, Object&gt; avp : params.getEntries())
 * {
 * String key = avp.getKey();
 * // Skip key-value pairs that the DataDescriptor specially manages.
 * if (key.equals(AVKey.FILE_STORE_LOCATION)
 * || key.equals(AVKey.DATA_CACHE_NAME)
 * || key.equals(AVKey.DATASET_NAME)
 * || key.equals(AVKey.DATA_TYPE))
 * {
 * continue;
 * }
 *
 * descriptor.setValue(key, avp.getValue());
 * }
 *
 * DataDescriptorWriter writer = new BasicDataDescriptorWriter();
 * writer.setDestination(installLocation);
 * writer.write(descriptor);
 * </code>
 * </pre>
 * </blockquote>
 * </li>
 * <li><strong>Searching for DataDescriptor Files in the WorldWind FileStore</strong>
 * <br>
 * The following code snippet is an example of how FileStore.findDataDescriptors was used to search for DataDescriptor
 * files in the WorldWind FileStore. <a href="#Example_8">Example 8</a> shows how to replace this with usage of data
 * configuration documents.
 * <blockquote>
 * <pre>
 * <code>
 * // Given a WorldWind FileStore location in which to look for DataDescriptor files, return a list of cache names
 * // representing the matches closest to the root: DataDescriptor files who's ancestor directories contain another
 * // DataDescriptor file are ignored. The search location must not be null.
 * String fileStoreLocation = ...;
 * FileStore fileStore = ...;
 * List&lt;? extends DataDescriptor&gt; dataDescriptors = fileStore.findDataDescriptors(fileStoreLocation);
 *
 * if (dataDescriptors == null || dataDescriptors.size() == 0)
 * {
 * // No DataDescriptor files found in the specified FileStore location.
 * return;
 * }
 * </code>
 * </pre>
 * </blockquote>
 * </li>
 * <li><strong>Creating WorldWind Components from DataDescriptor</strong>
 * <br>
 * The following code snippet is an example of how the AVList parameters attached to DataDescriptor were used to
 * construct WorldWind Layers and ElevationModels. <a href="#Example_9">Example 9</a> shows how to replace this with
 * usage of data configuration documents.
 * <blockquote>
 * <pre>
 * <code>
 * // Given a reference to a DataDescriptor which describes tiled imagery or elevations in the WorldWind
 * // FileStore, create a WorldWind Layer or ElevationModel according to the contents of the DataDescriptor.
 * DataDescriptor dataDescriptor = ...;
 *
 * if (dataDescriptor.getType().equals(AVKey.TILED_IMAGERY))
 * {
 * BasicTiledImageLayer layer = new BasicTiledImageLayer(dataDescriptor);
 * layer.setNetworkRetrievalEnabled(false);
 * layer.setUseTransparentTextures(true);
 * if (dataDescriptor.getName() != null)
 * {
 * layer.setName(dataDescriptor.getName());
 * }
 * }
 * else if (dataDescriptor.getType().equals(AVKey.TILED_ELEVATIONS))
 * {
 * // DataDescriptor files do not contain properties describing an ElevationModel's extreme elevations. Give
 * // those properties default values using the known extreme elevations on Earth.
 * dataDescriptor.setValue(AVKey.ELEVATION_MIN, Earth.ELEVATION_MIN);
 * dataDescriptor.setValue(AVKey.ELEVATION_MAX, Earth.ELEVATION_MAX);
 *
 * // DataDescriptor files contain the property key "gov.nasa.worldwind.avkey.MissingDataValue", which must be
 * // translated to AVKey.MISSING_DATA_SIGNAL so it will be understood by BasicElevationModel.
 * if (dataDescriptor.hasKey("gov.nasa.worldwind.avkey.MissingDataValue"))
 * {
 * Object missingDataSignal = dataDescriptor.getValue("gov.nasa.worldwind.avkey.MissingDataValue");
 * dataDescriptor.removeKey("gov.nasa.worldwind.avkey.MissingDataValue");
 * dataDescriptor.setValue(AVKey.MISSING_DATA_SIGNAL, missingDataSignal);
 * }
 *
 * BasicElevationModel elevationModel = new BasicElevationModel(dataDescriptor);
 * elevationModel.setNetworkRetrievalEnabled(false);
 * if (dataDescriptor.getName() != null)
 * {
 * elevationModel.setName(dataDescriptor.getName());
 * }
 * }
 * </code>
 * </pre>
 * </blockquote>
 * </li>
 * </ul>
 *
 *
 */
package gov.nasa.worldwind.data;
