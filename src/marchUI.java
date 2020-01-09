import java.io.*;
import java.util.LinkedList;
import java.util.ListIterator;

public class marchUI{
	public static String outputPath = "/Users/hanlinwang/Desktop/thesis3/MyProgram/external_link/";
	public static void File_Output(String path, LinkedList<String> content){
		System.out.println("Done saved in " + path);
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path, false)); //overwrite 
			bufferedWriter.write("[");
			ListIterator Iter = content.listIterator();
			while (Iter.hasNext()){
				String s = (String) Iter.next();
				bufferedWriter.write(s + "\n");
			}
			bufferedWriter.write("]");
			bufferedWriter.close();
		} catch (IOException e) {
			System.out.println("Outpot Error!!  " + e);
		}
	}
	
	static LinkedList<String> CurrentOutput = new LinkedList<String>();
	public static void Tree_Output(viewtree currentP, LinkedList<viewtree> widgets){
		int indexP = widgets.indexOf(currentP) +1;
		String newline = "\"" + indexP + "\":{"; 
		CurrentOutput.add(newline);
		newline = "\"Position\": \"" + currentP.content.position + "\",";
		CurrentOutput.add(newline);
		newline = "\"Class\": \"" + currentP.content.widgetClass + "\",";
		CurrentOutput.add(newline);
		newline = "\"Content\": \"" + currentP.content.content + "\",";
		CurrentOutput.add(newline);
		newline = "\"Children\": {" ;
		CurrentOutput.add(newline);
		int i =0;
		while (i < currentP.children.size()) {
			Tree_Output(currentP.children.get(i), widgets);
			if (i == currentP.children.size() -1) {
				newline = "}";
			} else {
				newline = "},";
				
			}
			CurrentOutput.add(newline); // this one for close index inside "Children"
			i++;
		}		
		CurrentOutput.add("}"); // this one for close "Children"
		return ;
	}
	

	public static void main(String arg[]){
		
		
		String FilesPath = "/Users/hanlinwang/Desktop/thesis3/UIRenderingJar/output/ui_output/";
		
		File files = new File(FilesPath);
		File[] FilesArray = files.listFiles();
		String NewLine = "";
		// Go through all APK
		for (int i =0; i< FilesArray.length; i++){
			// One APK per folder
			if (FilesArray[i].isDirectory()){
				System.out.println("=======" + FilesArray[i].getName() + "============");
				
				String path = FilesArray[i].getPath(); // Path of current APK
				//path = "/Users/hanlinwang/Desktop/thesis3/UIRenderingJar/output/ui_output/us.lovinghut.android.apk";
				File file = new File(path);
				File[] array = file.listFiles();
				CurrentOutput = new LinkedList<String>();
				
				for (int j=0; j < array.length; j++){
					
					if (array[j].isFile()){
						LinkedList<viewtree> widgets = new LinkedList<viewtree>();
						viewtree treeRoot = new viewtree(); 
						String filename = array[j].getName();
						String[] Aname = filename.split("-");
						String[] ActivityName = Aname[1].split("\\.");
						
						CurrentOutput.add("{");
						
						NewLine = "\"Name\":" + "\""+ ActivityName[0]+ "\",";
						CurrentOutput.add(NewLine);

						NewLine = "\"Screenshot\":" + "\".screenshot/" + FilesArray[i].getName() + '/' + filename + "\",";
						CurrentOutput.add(NewLine);						

						
						String dotpath = "/Users/hanlinwang/Desktop/thesis3/UIRenderingJar/output/dot_output/"+ FilesArray[i].getName() + "/" + ActivityName[0]+".dot";
						//dotpath = "/Users/hanlinwang/Desktop/thesis3/UIRenderingJar/output/dot_output/us.lovinghut.android.apk/" + "/" + ActivityName[0]+".dot";
						File dotfile = new File(dotpath);
						InputStreamReader read;
						try {
							
							// Save all lines in our variables
							read = new InputStreamReader(new FileInputStream(dotfile), "UTF-8");
							BufferedReader bufferedReader = new BufferedReader(read);
							LinkedList<String> lines = new LinkedList<String>();
							String s = null;
							while ((s = bufferedReader.readLine()) != null) {
								//if (s.contains(" -> ")) break;	
								if (s.contains("}")) break;
								lines.add(s);
							}
							bufferedReader.close();
							read.close();
							
							lines.remove(0);
							String linetxt = null;
							int len = 0;
							
							//NewLine = "\"Widget List\":" + "[";
							//CurrentOutput.add(NewLine);	
							
							while (len < lines.size()){
								linetxt = lines.get(len);
								
								// Content part
								if (!linetxt.contains(" -> ")) {
									int br = 0, charPos = 0;
									char[] c =linetxt.toCharArray();
									String Position = ""; 
									while (charPos<c.length && br!=2){
										if (c[charPos] == '(') {
											while (br!=2) {
												Position = Position + c[charPos];
												charPos ++;
												if (charPos ==c.length) break;
												if ( c[charPos] == ')'){
													br ++;
												}
											}
										}
										charPos ++;
									}
									Position =  Position + ")";
	
									//CurrentOutput.add(Position);
									
									String WidgetName = "";
									charPos ++;
									while (charPos<c.length && c[charPos]!=':'){
										WidgetName = WidgetName + c[charPos];
										charPos ++;
									}
									  
									//CurrentOutput.add(WidgetName);
									
									String Content = "";
									Boolean End = false;
									while (!End) {
										
										while (charPos<c.length && c[charPos]!='"'){
											if (c[charPos]!=':') { Content = Content + c[charPos];}
											charPos ++;
										}
										if (charPos >= c.length){
											// Content more than one line 
											charPos = 0;
											len ++;
											linetxt = lines.get(len);
											c = linetxt.toCharArray();
										} else {
											Content = Content.replace('\\', '|'); // in case Json bug
											widgetContent currentWidget = new widgetContent();
											currentWidget.content = Content;
											currentWidget.position = Position;
											currentWidget.widgetClass = WidgetName;
											
											viewtree newPoint = new viewtree();
											newPoint.content = currentWidget;
											newPoint.children = new LinkedList<viewtree>();
											widgets.add(newPoint);
											
											//if (len >= lines.size() -1) CurrentOutput.add( Position + WidgetName + Content + "\"]");
											//else CurrentOutput.add( Position + WidgetName + Content + "\",");
											End = true; 
										}
									}
								} else {
									// View Tree part
									String[] getIt = linetxt.split(" -> ");
									String parent = getIt[0].trim();
									String child = getIt[1].substring(0, getIt[1].length() -1);
									int par = Integer.valueOf(parent) -1;
									int chi = Integer.valueOf(child) -1;
									viewtree vtp = widgets.get(par);
									viewtree vtc = widgets.get(chi);
									vtp.children.add(vtc);
								}
								
								len ++; // Go to next line
							}
							
							// ViewTree output
							treeRoot = widgets.get(0);
							Tree_Output(treeRoot, widgets);
							CurrentOutput.add("}"); // close the index of root
							if (j != array.length -1) CurrentOutput.add("},"); else CurrentOutput.add("}");

							} catch (IOException e) {
							// IO Exception 
							System.out.println(dotpath + "  IO Error! " + e);
						}

						
					}
				} 		
				

				File_Output(outputPath + FilesArray[i].getName() + ".json", CurrentOutput);
				//if (i==0) {return ;}
			} 
			// One APK done
			
			
		}
		
		
		
	}
}