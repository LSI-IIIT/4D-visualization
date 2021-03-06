package citygmlModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.citygml4j.CityGMLContext;
import org.citygml4j.builder.CityGMLBuilder;
import org.citygml4j.builder.copy.CopyBuilder;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.Building;
import org.citygml4j.model.citygml.building.GroundSurface;
import org.citygml4j.model.citygml.building.RoofSurface;
import org.citygml4j.model.citygml.building.WallSurface;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.feature.BoundingShape;
import org.citygml4j.model.gml.geometry.primitives.Coord;
import org.citygml4j.model.gml.geometry.primitives.DirectPosition;
import org.citygml4j.model.gml.geometry.primitives.DirectPositionList;
import org.citygml4j.model.gml.geometry.primitives.Envelope;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.PosOrPointPropertyOrPointRep;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.citygml4j.util.walker.FeatureWalker;
import org.citygml4j.util.walker.GMLWalker;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.reader.CityGMLReader;

public class MultipleBuildingsFileClass {
	private List<BuildingsClass> buildingsList;
	SurfaceMember surfaceMember = new SurfaceMember();
	List<SurfaceMember> surfaceMemberList = new ArrayList<SurfaceMember>();
	List<SurfaceMember> wallList = new ArrayList<SurfaceMember>();
	List<SurfaceMember> roofList = new ArrayList<SurfaceMember>();
	List<SurfaceMember> solidList = new ArrayList<SurfaceMember>();
	public List<Envelope> envelopeList = new ArrayList<Envelope>();
	Envelope newEnv = new Envelope();
	String crs;
	String layerName;
	

	String typeFlag;
	

	public String getLayerName() {
		return layerName;
	}
	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}

	public String getCrs() {
		return crs;
	}
	public void setCrs(String crs) {
		this.crs = crs;
	}
	public MultipleBuildingsFileClass(){
		this.crs="";
		this.buildingsList = new ArrayList<BuildingsClass>();
	}
	public void IterateGMLFile(String filepath) throws Exception{
		
		System.out.println("filepath:"+filepath);
		CityGMLContext ctx = new CityGMLContext();
		CityGMLBuilder builder = ctx.createCityGMLBuilder();
		
		CityGMLInputFactory in = builder.createCityGMLInputFactory();
		try{
			CityGMLReader reader = in.createCityGMLReader(new File(filepath));
			System.out.println("Going to read the data, value of hasNext element:"+reader.hasNext());
			while(reader.hasNext()){
				CityGML citygml = reader.nextFeature();
				System.out.println("Found class:" + citygml.getCityGMLClass() + "\nVersion"+citygml.getCityGMLModule().getVersion());
				if(citygml.getCityGMLClass() == CityGMLClass.CITY_MODEL)
				{
					CityModel cityModel = (CityModel)citygml;
					
					
					//FeatureWalker buildingWalker = IterateBuildings();
					//cityModel.accept(buildingWalker);
					
					//A visitor Iterates over all the element in the entire file 
					BuildingsClass building = new BuildingsClass();
					
					//Get the crs
					//AbstractFeature feature = (AbstractFeature)cityModel;
					BoundingShape bound = cityModel.getBoundedBy();
					
					this.typeFlag="groundSurface";
					FeatureWalker groundWalker = IterateGroundSurface(building);
					cityModel.accept(groundWalker);
					
					this.typeFlag="walls";
					FeatureWalker wallWalker = IterateWall(building);
					cityModel.accept(wallWalker);
					
					this.typeFlag="roofs";
					FeatureWalker roofWalker = IterateRoof(building);
					cityModel.accept(roofWalker);
					
					this.typeFlag="solid";
					FeatureWalker solidWalker = IterateSolid(building);
					cityModel.accept(solidWalker);
					
					//FeatureWalker buildingWalker = IterateBuildings(building);
					//cityModel.accept(buildingWalker);
					
					this.buildingsList.add(building);
					
				}
				else
				{
					System.out.println("The gml file doesn't have a CITY_MODEL");
				}
			}
		}
		catch(Exception e){
			System.out.println("Error in Reading the CityGML file");
			e.printStackTrace();
		}
	}
	private FeatureWalker IterateBuildings(BuildingsClass singleBuilding){
		FeatureWalker buildingWalker = new FeatureWalker(){
			public void visit(Building building){
				
				BoundingShape bound = building.getBoundedBy();
				
				if(bound.isSetEnvelope()){
					Envelope env = bound.getEnvelope();
					System.out.println("crs:"+env.getSrsName());
					newEnv = new Envelope();
					newEnv.setSrsName(env.getSrsName());
					//CopyBuilder builder = null;
					//builder.copy(newEnv);
					//env.copyTo(newEnv, builder);
					System.out.println(newEnv.getSrsName());
					envelopeList.add(newEnv);
				}
			}
		};

		singleBuilding.setEnvolope(newEnv);
		return buildingWalker;
	}

	private FeatureWalker IterateGroundSurface(BuildingsClass singleBuilding){
		
		FeatureWalker groundWalker = new FeatureWalker(){
			
			public void visit(GroundSurface groundSurface){
				GMLWalker gmlWalker = new GMLWalker(){
					public void visit(LinearRing linearRing){
						
						/*if(linearRing.isSetPosList()){
							DirectPositionList posList = linearRing.getPosList();
							List<Double> points = posList.toList3d();
							
							List<CoordinateClass> polygonfloor = new ArrayList<CoordinateClass>();
							PolygonClass poly = new PolygonClass();
							for(int i=0 ; i<points.size() ;i+=3){
								double[] vals = new double[]{points.get(i) , points.get(i+1),points.get(i+2)};
								//System.out.println(vals[0]+" "+vals[1]+" "+vals[2]);
								CoordinateClass coord = new CoordinateClass(vals);
								polygonfloor.add(coord);
							}
							poly.setPolygon(polygonfloor);
							surfaceMember.setPolygon(poly);
							surfaceMemberList.add(surfaceMember);
							surfaceMember = new SurfaceMember();
							
						}*/
						visitMethod(linearRing);
						//VisitMethod2(linearRing);
					}
				};
				groundSurface.accept(gmlWalker);
			}
		};
		singleBuilding.setSurfacePolygon(surfaceMemberList);
		
		return groundWalker;
	}
	
	private FeatureWalker IterateWall(BuildingsClass singleBuilding){
		
		FeatureWalker wallWalker = new FeatureWalker(){
			
			public void visit(WallSurface wall){
				GMLWalker gmlWalker = new GMLWalker(){
					public void visit(LinearRing linearRing){
						visitMethod(linearRing);
					}
				};
				wall.accept(gmlWalker);
			}
		};
		singleBuilding.setWalls(wallList);
		//wallList = new ArrayList<SurfaceMember>();
		return wallWalker;
	}
	
	private FeatureWalker IterateRoof(BuildingsClass singleBuilding){
		
		FeatureWalker roofWalker = new FeatureWalker(){
			
			public void visit(RoofSurface roof){
				GMLWalker gmlWalker = new GMLWalker(){
					public void visit(LinearRing linearRing){
						visitMethod(linearRing);
					}
				};
				roof.accept(gmlWalker);
			}
		};
		singleBuilding.setRoofs(roofList);
		return roofWalker;
	}
	
	private FeatureWalker IterateSolid(BuildingsClass singleBuilding){
		
		FeatureWalker solidWalker = new FeatureWalker(){
			
			public void visit(Building building){
				//Bounding
				/*BoundingShape bound = building.getBoundedBy();
				if(bound.isSetEnvelope()){
					Envelope evn = bound.getEnvelope();
					evn.getSrsName();
					crs = evn.getSrsName();
					System.out.println(crs);
				}
				else{
					System.out.println("The crsString is not set");
				}*/
				GMLWalker Walker = new GMLWalker(){
					public void visit(Solid solid){
						if(solid.isSetExterior()){
							GMLWalker gmlWalker = new GMLWalker(){
								public void visit(LinearRing linearRing){
									visitMethod(linearRing);
								}
							};
							solid.accept(gmlWalker);
						}
					}
				};
				building.accept(Walker);
			}
		
		};
		singleBuilding.setSolid(solidList);
		return solidWalker;
	}

	
	private void visitMethod(LinearRing linearRing){
		if(linearRing.isSetPosList()){
			DirectPositionList posList = linearRing.getPosList();
			List<Double> points = posList.toList3d();
			
			List<CoordinateClass> polygonfloor = new ArrayList<CoordinateClass>();
			PolygonClass poly = new PolygonClass();
			for(int i=0 ; i<points.size() ;i+=3){
				double[] vals = new double[]{points.get(i) , points.get(i+1),points.get(i+2)};
				System.out.println(vals[0]+" "+vals[1]+" "+vals[2]);
				CoordinateClass coord = new CoordinateClass(vals);
				polygonfloor.add(coord);
			}
			poly.setPolygon(polygonfloor);
			
			surfaceMember.setPolygon(poly);
			if(this.typeFlag=="groundSurface")
				surfaceMemberList.add(surfaceMember);
			else if(this.typeFlag=="walls")
				wallList.add(surfaceMember);
			else if(this.typeFlag=="roofs")
				roofList.add(surfaceMember);
			else if(this.typeFlag=="solid")
				solidList.add(surfaceMember);
			surfaceMember = new SurfaceMember();
			//singleBuilding.setSurfacePolygon(surfaceMember);
			
			//surfacePolygons.add(buildingSurfacePolygon);
		}
		else{
			
			List<PosOrPointPropertyOrPointRep> posList = linearRing.getPosOrPointPropertyOrPointRep();
			for(PosOrPointPropertyOrPointRep position : posList){
				DirectPosition pos = position.getPos();
				List<Double> points = pos.getValue();
				
				List<CoordinateClass> polygonfloor = new ArrayList<CoordinateClass>();
				PolygonClass poly = new PolygonClass();
				for(int i=0 ; i<points.size() ;i+=3){
					double[] vals = new double[]{points.get(i) , points.get(i+1),points.get(i+2)};
					//System.out.println(vals[0]+" "+vals[1]+" "+vals[2]);
					
					CoordinateClass coord = new CoordinateClass(vals);
					polygonfloor.add(coord);
				}
				poly.setPolygon(polygonfloor);
				
				surfaceMember.setPolygon(poly);
				if(this.typeFlag=="groundSurface")
					surfaceMemberList.add(surfaceMember);
				else if(this.typeFlag=="walls")
					wallList.add(surfaceMember);
				else if(this.typeFlag=="roofs")
					roofList.add(surfaceMember);
				surfaceMember = new SurfaceMember();
				
			}
			//System.out.println("Its not a PosList :(");
		}
	}

	public List<BuildingsClass> getBuildingsList() {
		return buildingsList;
	}

	public void setBuildingsList(List<BuildingsClass> buildingsList) {
		this.buildingsList = buildingsList;
	}
	

		
	
}
