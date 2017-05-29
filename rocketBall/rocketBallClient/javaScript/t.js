var JavaPackages = new JavaImporter(
		Packages.sage.scene.SceneNode,
		Packages.sage.terrain.ImageBasedHeightMap,
		Packages.sage.texture.Texture,
		Packages.sage.texture.TextureManager,
		Packages.graphicslib3D.Vector3D,
		Packages.graphicslib3D.Point3D,
		Packages.sage.terrain.TerrainBlock,
		Packages.java.io.File
);
with (JavaPackages) {
	var heightMap = new ImageBasedHeightMap("images/heightMaps/height.jpg");

	var heightScale = 0.1;
	var terrainScale = new Vector3D(5, heightScale, 5);
	var terrainSize = heightMap.getSize();
	var cornerHeight = heightMap.getTrueHeightAtPoint(0, 0) * heightScale;
	var terrainOrigin = new Point3D(0, -cornerHeight, 0);
	var terrain = new TerrainBlock("terrain", terrainSize, terrainScale, heightMap.getHeightData(), terrainOrigin);
	
	
	var grassTexture = TextureManager.loadTexture2D("images/textures/bkgd1.jpg");
	
}