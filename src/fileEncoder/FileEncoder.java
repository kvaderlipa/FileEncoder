package fileEncoder;
import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import javax.imageio.ImageIO;

/**
 * 
 * @author 4900063
 *
 * TODO
 * DONE urob enc/dec do morseovej abecedy, 
 * DONE				v buffry by citalo po pismene, nie po riadku (ak by bola neriadkovana, by padlo na pamat)
 * DONE				ak enter, tak zapis enter, 
 * DONE				ak nenajde, tak error a napisat co
 * DONE				oddelovac pismen ' ' slov '/'
 * source	https://morsecode.world/international/morse2.html
 * 
 * este by sa dalo zrychlit tak, zevypocita kolko bitov a maticou by preslo iba raz, teraz sa to toci po jednom bite cela matica,, cize ak napr. by vypocitalo 5bitov, tak by netocilo maticu 5 krat ale iba raz
 *DONE 		steg zrychlenie, urobit cez maticu intov, je to rychlejsie
 * 		 
 *  	IN PROGRESS potom pri stegano, mozno dat moznost, ze prekonvertuje subor na obrazok, urobi taky velky ako treba, dalo by sa potom rychlo takto do obrazka konvertovat ay sa to dalo cez web bez problemov posielat
 *  		- dorobit na adresare, ze ak adresar, tak vytvori adresar lakes, kde rekurzivne ako v delete vsetko zakoduje do obrazkov jazier, vyzera ale, ze pouzije na kazdy obrazok rovnaky seed, lebo neviem zabezpecit poradie kedze nazvy budu pomenene
 *  		- opacne tiez pre dekompresiu
 *  		- ak je velkost prilis velka, rozdelit, dat parameter, vymysliet ako by sa zabezpecilo, ze bude vediet kde pokracovat
 *DONE		- asi koli jednoduchosti, ak je -steg a za nim len jeden parameter, beriez, ze automaticky generovat, ak chcem specificky seed, tak dam dve pomlcky a seed - - 455
 *DOME		ak dame -steg -d .png tak dekoduje vstky subory konciace .png			
 *  
 *DONE bez adresarov 		porozmyslat nad one time pad s moznostou prepis, kde by priamo prepisovlo data, aby sa nedali spatne dostat spat, tu ma zmysel aj adresar, mozno tabulka s nazvami random
 * 		porozmyslat AES a prepis, co by bol ruunsomware, ;-)) a tiez rekurzivne na adresare, nahodne heslo a cez public key zasifrovat a vytvorit subor so zasifrovanym heslom
 * 		
 */

public class FileEncoder {
	
	private static boolean PRODUCTION = true;
	
	// variables for access to see actual progress and for earlier terminating
	static boolean inProgress = false;	//status if working or not
	static boolean cancel = false;		//signal for canceling process
	static int progress = 0; 			// 0 to 100%	
	//
	static final long DEFAULT_SEED = 4963;
	static final long INIT_CYCLES = 4963;
	static final int BUFFER_SIZE = 65536;
	
	static long cnt = 0;
	public static void GEN_SECURE_RANDOM_DATA_FILE(String output, final long size, int threadCount) throws Exception {
		//
		inProgress = true;
		cancel = false;
		progress = 0;
		cnt = 0;
		//
		
		if(threadCount>1024)
			threadCount = 1024;
		if(threadCount<1)
			threadCount = 1;

		final FileOutputStream fos = new FileOutputStream(new File(output));
		
		ArrayList<Thread> list = new ArrayList<Thread>();
		
		for (int i = 0; i < threadCount; i++)
			list.add(
			new Thread(i+""){				
		        public void run(){
		        	String bit_val;
		    		String bit_sleep;
		    		String pom;
		    		byte data;
		    		long sleep = 170L;
		    		long initCount = 10L;      
		    		int sum;
		    		while(cnt<size && !cancel) {
		    			//
		    			if(cnt%1000==1)
		    				progress = (int)((double)cnt/size*100);		    			
		    			//		    			
		    			bit_val = "";
		    			bit_sleep = "";
		    			for (int i = 0; i < 8; i++) {
		    				try{
		    					Thread.sleep(sleep);
		    				}catch(Exception ex) {
		    					ex.printStackTrace();
		    				}
		    				pom = System.nanoTime()+"";		    				
		    				sum = 0;
		    				for (int j = pom.length()-8; j < pom.length(); j++) 
		    					sum += Integer.parseInt(pom.charAt(j)+"");
		    				if(sum % 2 == 0)
		    					bit_val += "0";
		    				else
		    					bit_val += "1";
		    				try {
		    					Thread.sleep(sleep);
		    				}catch(Exception ex) {
		    					ex.printStackTrace();
		    				}
		    				pom = System.nanoTime()+"";
		    				sum = 0;
		    				for (int j = pom.length()-8; j < pom.length(); j++) 
		    					sum += Integer.parseInt(pom.charAt(j)+"");		    					
		    				if(sum % 2 == 0)
		    					bit_sleep += "0";
		    				else
		    					bit_sleep += "1";
		    				//System.out.println(pom);
		    				//System.out.println("bit_val "+bit_val);
		    				//System.out.println("bit_sleep "+bit_sleep);
		    			}		
		    			sleep = Long.parseLong(bit_sleep, 2)+170L;
		    			data = (byte)Integer.parseInt(bit_val, 2);	
		    			if(initCount--<=0) {
		    				try {		    	
		    					synchronized (this) {
			    					if(cnt<size && !cancel) {
			    						fos.write(data);
			    						cnt++;		    					
			    						//System.out.println("Generated "+cnt+" bytes ("+getName()+")");
			    					}
		    					}
		    				}catch(Exception ex) {
		    					ex.printStackTrace();
		    				}		    				
		    			}
		    		}
		        }
		      });		      
		     
		for (int i = 0; i < list.size(); i++) {
			Thread.sleep(170);
			if(i==list.size()-1)
				list.get(i).run(); // run do not evoke new thread what is desired (saves one thread for closing stream) ***GENIUS
			else
				list.get(i).start();
		}
					    	  
		while(cnt<size && !cancel)
			Thread.sleep(170);
		fos.close();
		//
		inProgress = false;
		//
	}
	
	public static void GEN_RANDOM_DATA_FILE(String output, long size, Random r) throws IOException {
		//
		inProgress = true;
		cancel = false;
		progress = 0;
		//		
		
		byte[] buffer = new byte[BUFFER_SIZE];
		
		// initialize
		for (int i = 0; i < INIT_CYCLES; i++)
			r.nextBytes(buffer);
		
		long i = 0;
		FileOutputStream fos = new FileOutputStream(new File(output));
		
		while(i<size && !cancel) {
			r.nextBytes(buffer); // tak toto bol riaden bug ze to tu chybalo
			i += buffer.length;
			if(i<size)
				fos.write(buffer, 0, buffer.length);
			else
				fos.write(buffer, 0, (int)(size-(i-buffer.length)));
			progress = (int)((double)i/size*100);
		}
		
		fos.close();
		//		
		inProgress = false;
		//
	}
	
	private static long totalSize;
	/**
	 * private recursive method, get all files/folders in path, rename to random and calculate total size
	 */
	private static void getDirFiles(File file, List<File> list, Random r)throws IOException{		
		String name;
		File[] files;
		File file2;
		if(file.isDirectory()){
			name = "";
			for (int i = 0; i < 16; i++)
				name += r.nextInt(10);
			file2 = new File(file.getParent()+File.separatorChar+name);
			if(!file.renameTo(file2))
				throw new IOException("File/Folder name already exists, secure delete not successful");
			file = file2;
		    // get file and DIR list
			files = file.listFiles();
			if(files!=null)
				for (File f : files) {
					getDirFiles(f, list, r);						
				}
		}else{
			totalSize += file.length();
			//rename file name
			name = "";
			for (int i = 0; i < 16; i++)
				if(i!=12)
					name += r.nextInt(10);
				else
					name += ".";
			file2 = new File(file.getParent()+File.separatorChar+name);
			if(!file.renameTo(file2))
				throw new IOException("File/Folder name already exists, secure delete not successful");
			list.add(file2);
		}
	} 

	public static void SECURE_DELETE(String input, Random r) throws IOException {
		//
		inProgress = true;
		cancel = false;
		progress = 0;
		//		
		byte[] buffer = new byte[BUFFER_SIZE];
		List<File> list = new LinkedList<File>();
		totalSize = 0;//will be filled by total size after getDirFiles is called
		File file = new File(input);
		file = new File(file.getCanonicalPath());
		if(file.exists()){
			getDirFiles(file, list, r);
	
			long actualSize = 0;
			int i;
					
			for (File f : list) {						
		        RandomAccessFile racr = new RandomAccessFile(f, "r");
		        RandomAccessFile racw = new RandomAccessFile(f, "rw");
		                
				while((i=racr.read(buffer))!=-1 && !cancel) {				
					r.nextBytes(buffer);				
					racw.write(buffer, 0, i);
					actualSize +=i;
					progress = (int)((double)actualSize/totalSize*100);
				}
				racr.close();
				racw.close();
				if(cancel)
					break;
			}
		}else 
			throw new IOException("File/Folder ("+input+") does not exists, secure delete not successful");
		//		
		inProgress = false;
		//		
	}
	
	public static void ONE_TIME_PAD(String input, String output, Random r) throws IOException {
		//
		inProgress = true;
		cancel = false;
		progress = 0;
				
		
        //long time = System.currentTimeMillis();
        
		byte[] buffer = new byte[BUFFER_SIZE];
		byte[] xor = new byte[buffer.length];
		
		// initialize
		for (int i = 0; i < INIT_CYCLES; i++)
			r.nextBytes(xor);
		
		int i;
                RandomAccessFile racr = new RandomAccessFile(input, "r");
                RandomAccessFile racw = new RandomAccessFile(output, "rw");
                
		while((i=racr.read(buffer))!=-1 && !cancel) {
			r.nextBytes(xor);
			for (int j = 0; j < i; j++)
				buffer[j] ^= xor[j]; 
			racw.write(buffer, 0, i);
			progress = (int)((double)racr.getFilePointer()/racr.length()*100); //tested, does not slow down really
		}
		racr.close();
		racw.close();	
		//		
		inProgress = false;
		//
		
		//System.out.println(System.currentTimeMillis()-time+" milis took to end file");
	}
	
		public static BufferedImage CREATE_IMAGE(int width, int height, int red, int green, int blue, Random r){
			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			int bgr = (red<<16)+(green<<8)+blue;
			
			for(int x=0;x<width;x++)
				for(int y=0;y<height;y++)
					bi.setRGB(x, y, r!=null?r.nextInt():bgr);			
			return bi;
		}
				 
        public static int ENCODE_FILE_TO_IMAGE(String input, String imageFile, String output, long seed) throws Exception{
        	//
    		inProgress = true;
    		cancel = false;
    		progress = 0;
    		cnt = 0;
    		//
        	BufferedImage img = null;
        	if(!imageFile.equals("-"))
	            try {
	                img = ImageIO.read(new File(imageFile));
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
        	        	        	
        	ImageByteStreamWriter ibsw = null;
        	String origOutput = output;
        	String origInput = input;
        	RandomAccessFile raf = new RandomAccessFile(input, "r");
        	RAFBuffer rafB = new RAFBuffer(raf);
        	        	
        	//0 - lakes, 1 - stars
        	int autoImgType = RANDOM.nextInt(3);
        	//if needed to split determine parts count, if defined by user output image, must be managed by user
        	int parts = 1;
        	if(img!=null){
        		//parts = (int)Math.ceil((double)raf.length()/(img.getWidth()*img.getHeight()*3+260));
        	}else{
        		parts = (int)Math.ceil((double)raf.length()/MAX_FILE_SIZE);        		
        	} 
        	
        	long time = System.currentTimeMillis();
        	for(int i=0;i<parts;i++){
	        	//if not defined, random lake name
	        	if(origOutput.equals("-")){
	        		int counter = 0;
	        		String file;
	        		switch(autoImgType){
	        			case 0: file = LAKES[RANDOM.nextInt(LAKES.length)];
	        				break;
	        			case 1: file = STARS[RANDOM.nextInt(STARS.length)];
	        				break;
	        			default: file = MOON_ARTEFACTS[RANDOM.nextInt(MOON_ARTEFACTS.length)];
	        				break;
	        		}
	        		do{
	        			output = file+(counter>0?("_"+counter):"")+".png";	        			
	        			counter++;
	        		}while(new File(output).exists());	        		
	        	}
	        	        	
	            //if not defined created as lake
	            if(img==null){
	            	long  size = raf.length()/3;
	            	if(size<MIN_FILE_SIZE)
	            		size = MIN_FILE_SIZE;
	            	if(parts>1)
	            		size = MAX_FILE_SIZE/3;
	        		size *= 1/((MAX_DATA_IN_IMAGE_RATIO-MIN_DATA_IN_IMAGE_RATIO)*RANDOM.nextDouble()+MIN_DATA_IN_IMAGE_RATIO );
	        		
	        		int height = (int)Math.ceil(Math.sqrt(size/WIDTH_HEIGHT_RATIO));  
	        		int width = (int)Math.ceil(height*WIDTH_HEIGHT_RATIO);
	        		
	        		int shade = -MAX_SHADE+RANDOM.nextInt(MAX_SHADE*2);
	        		
		            time = System.currentTimeMillis();
		            switch(autoImgType){
	        			case 0: img = CREATE_IMAGE(width, height, LAKE_WATER_RGB[0]+shade, LAKE_WATER_RGB[1]+shade, LAKE_WATER_RGB[2]+shade, null);
	        				break;
	        			case 1: img = CREATE_IMAGE(width, height, STAR_SURFACE_RGB[0], STAR_SURFACE_RGB[1]+shade, STAR_SURFACE_RGB[2], null); //pri hviezdach tocit iba s GREEN
	        				break;
	        			default: img = CREATE_IMAGE(width, height, MOON_ARTEFACT_RGB[0]+shade, MOON_ARTEFACT_RGB[1]+shade, MOON_ARTEFACT_RGB[2]+shade, null);
	        				break;
	        		}
		            	
	        		/*
	        		System.out.println(System.currentTimeMillis()-time+" milis took create empty image");
		            time = System.currentTimeMillis();
		            */
	            }
	            
	            ibsw = new ImageByteStreamWriter(img, new Random(seed));
	            int b;
	            int size = (int)raf.length();
	            if(parts>1){
	            	if(i<parts-1)
	            		size = (int)MAX_FILE_SIZE;
	            	else
	            		size = (int)(raf.length()%MAX_FILE_SIZE);
	            	
	            }
	            //write file length
	            ibsw.write(size&255);
	            ibsw.write(size>>8&255);
	            ibsw.write(size>>16&255);
	            ibsw.write(size>>24&255);
	            input = new File(origInput).getName();
	            if(parts>1){
	            	input+="."+String.format("%0"+(parts+"").length()+"d", i+1);
	            }
	            //write name length
	            size = input.length();
	            ibsw.write(size&255);
	            //write name of file
	            for (int j = 0; j < input.length(); j++)
	                ibsw.write((int)input.charAt(j));
	            //write file data
	            long filePointer = 0;
	            time = System.currentTimeMillis();
	            while((b=rafB.read())!=-1 && !cancel){	            	
	                ibsw.write(b);
	                //progress = (int)((double)raf.getFilePointer()/raf.length()*100);
	                if(++filePointer==MAX_FILE_SIZE)
	                	break;
	            }
	            /*
	            System.out.println(System.currentTimeMillis()-time+" milis took trans matrix to image image");
	            time = System.currentTimeMillis();
	            */
	            ibsw.trans2img();
	            /*
	            System.out.println(System.currentTimeMillis()-time+" milis took write to image");
	            time = System.currentTimeMillis();
	            */            
	            try {
	                ImageIO.write(img, output.substring(output.lastIndexOf(".")+1), new File(output));
	                /*
	                System.out.println(System.currentTimeMillis()-time+" milis took write image to file");
		            time = System.currentTimeMillis();
		            */
	                System.out.println("file "+output+" created ("+(ibsw.getBitSize()+1)+" bits used)");
	                
	                /*
	                int[] matrix = new int[img.getWidth()*img.getHeight()];
	                
	            	img.getRGB(0, 0, img.getWidth(), img.getHeight(), matrix, 0, img.getWidth());	            	
	            	System.out.println(System.currentTimeMillis()-time+" milis took read whole matrix RGB");
		            time = System.currentTimeMillis();
		            
	            	img.setRGB(0, 0, img.getWidth(), img.getHeight(), matrix, 0, img.getWidth());
	            	System.out.println(System.currentTimeMillis()-time+" milis took write whole matrix RGB");
		            time = System.currentTimeMillis();
		            
		            ImageIO.write(img, output.substring(output.lastIndexOf(".")+1), new File("X_"+output));
		            System.out.println(System.currentTimeMillis()-time+" milis took write image to file");
		            time = System.currentTimeMillis();
	                */
	                img = null;
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
        	}        	
            raf.close();            
            //    		
    		inProgress = false;
    		//
            return ibsw.getBitSize()+1;
        }
        
        public static void DECODE_FILE_FROM_IMAGE(String imageFile, String outputPath, Random r) throws Exception{
    		//
    		inProgress = true;
    		cancel = false;
    		progress = 0;
    		cnt = 0;
    		//
        	RandomAccessFile raf;
            BufferedImage img = null;
            try {
                img = ImageIO.read(new File(imageFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
           
            ImageByteStreamReader ibsr = new ImageByteStreamReader(img, r);
            int size = 0;                        
            String name="";
            size = ibsr.read()|ibsr.read()<<8|ibsr.read()<<16|ibsr.read()<<24;
            
            int nameLength = ibsr.read();
            					//name length
            for (int i = 0; i < nameLength; i++)
                name += (char)ibsr.read();
            
            if(outputPath.length()>0){
            	outputPath = outputPath.replace("\\", "/");
            	if(outputPath.charAt(outputPath.length()-1)!='/')
					outputPath += "/";
            } 
            raf = new RandomAccessFile(outputPath+name, "rw");
            RAFBuffer rafb = new RAFBuffer(raf);
            for (int i = 0; i < size && !cancel; i++){
            	rafb.write(ibsr.read());   
            	//progress = (int)((double)i/size*100);
            }
            rafb.flush();
            raf.close();
            //    		
    		inProgress = false;
    		//
            /*
            System.out.println("size:"+size);
            System.out.println("nameLength:"+nameLength);
            System.out.println("name:"+name);
            */
        }
        
	public static void ONE_TIME_PAD_FILE_KEY(String input, String output, String key) throws IOException {
		//
		inProgress = true;
		cancel = false;
		progress = 0;
		//
		byte[] buffer = new byte[BUFFER_SIZE];
		byte[] xor = new byte[buffer.length];
		int i, k;
                RandomAccessFile raci = new RandomAccessFile(input, "r");
                RandomAccessFile rack = new RandomAccessFile(key, "r");
                RandomAccessFile raco = new RandomAccessFile(output, "rw");
                
		while((i=raci.read(buffer))!=-1 && !cancel) {			
			progress = (int)((double)raci.getFilePointer()/raci.length()*100);
			k = rack.read(xor, 0, i);
			if(k==-1) //in case end of file
				k=0;
			while(k<i) {				
				rack.seek(0);				
				k += rack.read(xor, k, i-k); 
			}
				
			for (int j = 0; j < i; j++)
				buffer[j] ^= xor[j]; 
			raco.write(buffer, 0, i);
		}
        raci.close();
		raco.close();		
		rack.close();
		//		
		inProgress = false;
		//
	}
	
	public static void BASE64enc(String input, String output) throws IOException {		
		//
		inProgress = true;
		cancel = false;
		progress = 0;
		cnt = 0;
		//
		byte[] buffer = new byte[49152];
		byte[] bufferOut = new byte[65536];
		
		int i, n=-1;
		
		File f = new File(input);
		FileInputStream fis = new FileInputStream(f);
		FileOutputStream fos = new FileOutputStream(new File(output));
		BufferedOutputStream bos = new BufferedOutputStream(fos); // because of new line according BASE64 spec
		while((i=fis.read(buffer))!=-1 && !cancel) {		
			if(i==buffer.length)
				i = Base64.getEncoder().encode(buffer, bufferOut);
			else
				i = Base64.getEncoder().encode(Arrays.copyOf(buffer, i), bufferOut);			
			
			for (int j = 0; j < i; j++) {
				if(n++ % 64 == 63)
					bos.write('\n');
				bos.write(bufferOut[j]);
			}
			//
			if(++cnt%10000==1)
				progress = (int)((double)cnt/f.length()*100);
			//
		}
		fis.close();
		bos.close();
		fos.close();
		//		
		inProgress = false;
		//
	}

	public static void BASE64dec(String input, String output) throws IOException {		
		//
		inProgress = true;
		cancel = false;
		progress = 0;
		cnt = 0;
		//
		byte[] buffer = new byte[65536];
		byte[] bufferOut = new byte[49152];
		
		int i, n=0;
		byte b;
		File f = new File(input);
		FileInputStream fis = new FileInputStream(f);
		BufferedInputStream bis = new BufferedInputStream(fis);
		FileOutputStream fos = new FileOutputStream(new File(output));	
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		
		while((i = bis.read())!=-1 && !cancel) {
			b =(byte)i;
			if((b>='A' && b<='Z')||(b>='a' && b<='z')||(b>='0' && b<='9')|| b=='+' || b=='/' || b=='=')				
				buffer[n++] = b;
			if(n==buffer.length) {
				n=0;
				i = Base64.getDecoder().decode(buffer, bufferOut);
				fos.write(bufferOut, 0, i);
			}
			//
			if(++cnt%10000==1)
				progress = (int)((double)cnt/f.length()*100);
			//
		}
		i = Base64.getDecoder().decode(Arrays.copyOf(buffer, n), bufferOut);
		fos.write(bufferOut, 0, i);
		
		bis.close();
		fis.close();		
		bos.close();
		fos.close();
		//		
		inProgress = false;
		//
	}

	private final static char[] HEX = new char[]{'0', '1', '2', '3', '4', '5', '6', '7','8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	
	public static void BASE16enc(String input, String output) throws IOException {		
		//
		inProgress = true;
		cancel = false;
		progress = 0;
		cnt = 0;
		//
		File f = new File(input);
		FileInputStream fis = new FileInputStream(f);
		FileOutputStream fos = new FileOutputStream(new File(output));
		BufferedInputStream bis = new BufferedInputStream(fis);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		
		int i, n=-1;
		while((i=bis.read())!=-1 && !cancel) {			
			for (int j = 1; j >= 0; j--) {
				if(n++ % 64 == 63)
					bos.write('\n');
				bos.write(HEX[((byte)i >> (j * 4)) & 0xF]);				
			}
			//
			if(++cnt%10000==1)
				progress = (int)((double)cnt/f.length()*100);
			//
		}
		bis.close();
		bos.close();
		//		
		inProgress = false;
		//
	}

	public static void BASE16dec(String input, String output) throws IOException {
		//
		inProgress = true;
		cancel = false;
		progress = 0;
		cnt = 0;
		//
		File f = new File(input);
		FileInputStream fis = new FileInputStream(f);
		FileOutputStream fos = new FileOutputStream(new File(output));
		BufferedInputStream bis = new BufferedInputStream(fis);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		
		int i, a;
				
		while((i=bis.read())!=-1 && !cancel) {			
			if(i=='\n')
				while((i=bis.read())=='\n');
			a = bis.read();
			if(a=='\n')
				while((a=bis.read())=='\n');
			//convert to upper case
			if(i >= 'a' && i <='f')
				i -= 'a' - 'A';				
			if(a >= 'a' && a <='f')
				a -= 'a' - 'A';
			//
            if (i >= '0' && i <= '9')
                i -= '0';
            else 
            	if (i >= 'A' && i <= 'F')
            		i -= 'A' - 10;
            
            if (a >= '0' && a <= '9')
                a -= '0';
            else 
            	if (a >= 'A' && a <= 'F')
            		a -= 'A' - 10;            
			bos.write((byte) ((i << 4) + a));

			//		
			cnt+=2;
			if(cnt%10000==1)
				progress = (int)((double)cnt/f.length()*100);
			//
		}
		bis.close();
		bos.close();
		//
		inProgress = false;
		//
	}
	
	public static void BASE2enc(String input, String output) throws IOException {	
		//
		inProgress = true;
		cancel = false;
		progress = 0;
		cnt = 0;
		//
		File f = new File(input);
		FileInputStream fis = new FileInputStream(f);
		FileOutputStream fos = new FileOutputStream(new File(output));
		BufferedInputStream bis = new BufferedInputStream(fis);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		
		int i, n =-1;
		byte[] pom;
		
		while((i=bis.read())!=-1 && !cancel) {			
			pom = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0').getBytes();
			if(n++ % 8 == 7)
				bos.write('\n');
			bos.write(pom);			
			//
			if(++cnt%10000==1)
				progress = (int)((double)cnt/f.length()*100);
			//
		}
		bis.close();
		bos.close();
		//		
		inProgress = false;
		//
	}

	public static void BASE2dec(String input, String output) throws IOException {	
		//
		inProgress = true;
		cancel = false;
		progress = 0;
		cnt = 0;
		//
		File f = new File(input);
		FileInputStream fis = new FileInputStream(f);
		FileOutputStream fos = new FileOutputStream(new File(output));
		BufferedInputStream bis = new BufferedInputStream(fis);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		
		int i, b=0;
		
		byte[] pom = new byte[8];
		while((i=bis.read())!=-1 && !cancel) {
			//		
			if(++cnt%10000==1)
				progress = (int)((double)cnt/f.length()*100);
			//
			if(i!='\n')
				pom[b++] = (byte)i;
			if(b<8)
				continue;
			else
				b = 0;
			bos.write((byte)Integer.parseUnsignedInt(new String(pom), 2));
		}
		bis.close();
		bos.close();
		//		
		inProgress = false;
		//
	}
	
	static class Tuple implements Comparable<Tuple>{
		String bin;
		long count = 0;
		@Override
		public int compareTo(Tuple o) {
				if(count>o.count)
					return 1;
				if(count<o.count)
					return -1;
			return 0;
		}
		public Tuple(String bin) {
			this.bin = bin;
		}
	}
	
	public static void GET_FILE_FREQUENCY(String input, PrintStream output, int n_bits_tuple) throws IOException {
		//
		inProgress = true;
		cancel = false;
		progress = 0;
		cnt = 0;
		//
		File f = new File(input);
		FileInputStream fis = new FileInputStream(f);		
		BufferedInputStream bis = new BufferedInputStream(fis);
		
		Map<String, Tuple> map = new TreeMap<String, Tuple>();
		int i;
		Tuple tuple;
		byte[] pom, buffer = new byte[n_bits_tuple];
		
		int pos = 0;

		while((i=bis.read())!=-1 && !cancel) {
			if(++cnt%10000==1)
				progress = (int)((double)cnt/f.length()*100);
			pom = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0').getBytes();
			for (int j = 0; j < pom.length; j++) {
				buffer[pos++] = pom[j];
				if(pos==buffer.length) {
					pos = 0;					
					tuple = map.get(new String(buffer));
					if(tuple==null) {
						tuple = new Tuple(new String(buffer));
						map.put(new String(buffer), tuple);
					}					
					tuple.count++;
				}
			}			
		}
		
		Collection<Tuple> values = map.values();
		
		ArrayList<Tuple> al = new ArrayList<Tuple>(values);
		Collections.sort(al, Collections.reverseOrder());
		bis.close();
		output.println("File size: "+f.length()+" Bytes");
		output.println("Tuples size: "+n_bits_tuple+" bits");
		output.println("Tuples count: "+al.size());
		double ent = 0, sum = 0;
		for (int j = 0; j < al.size(); j++)
			sum += al.get(j).count;
		for (int j = 0; j < al.size(); j++)
			ent += ((al.get(j).count/sum)*(Math.log(al.get(j).count/sum) / Math.log(2)));		
		output.println("Entropy: "+(ent*-1));
		for (int j = 0; j < al.size(); j++) {
			output.println(al.get(j).bin+"\t"+al.get(j).count);
		}		
		//		
		inProgress = false;
		//
	}
	
	public static void MORSEenc(String input, String output) throws IOException {		
		//
		inProgress = true;
		cancel = false;
		progress = 0;
		cnt = 0;
		//
		File f = new File(input);
		FileReader fr = new FileReader(f);
		FileWriter fw = new FileWriter(output);
		BufferedReader br = new BufferedReader(fr);
		BufferedWriter bw = new BufferedWriter(fw);
		
		int i;
		String str = null, str2;		
		while((i=br.read())!=-1 && !cancel) {
			//System.out.println((char)i);
			if(i=='\n' || i=='\r')
				bw.write(i);
			else{
				if(i==' '){
					if(str==null || !str.equals(" "))//prevent more white spaces						
						bw.write(MORSE_WORD_DELIMITER);
					str = (char)i+"";
				}else{
					if(str!=null && str.equals(" "))//to start with delimiter
						bw.write(MORSE_CHAR_DELIMITER);//new char start with delimiter
						
					str = ((char)i+"").toUpperCase();
					if((str2 = STRING_2_MORSE_MAP.get(str))!=null){
						bw.write(str2+MORSE_CHAR_DELIMITER);
					}else{						
						if(i=='<'){ //must begin and end with braces <>							
							while((i=br.read())!=-1 && i!='>' && str.length()<100){ //<100 because can fail on memmory because if you get < and MBs of next characters
								if(i==-1)
									throw new IOException("Error, there is no MORSE code:'"+str+"'");
								str += ((char)i+"").toUpperCase();
							}
							str += (char)i+"";
							if((str2 = STRING_2_MORSE_MAP.get(str.toUpperCase()))!=null)
								bw.write(str2+MORSE_CHAR_DELIMITER);
							else
								throw new IOException("Error, there is no MORSE code:'"+str+"'");
						}else{
							throw new IOException("Error, there is no MORSE code:'"+str+"'");
						}
					}
				}
			}
			//
			if(++cnt%10000==1)
				progress = (int)((double)cnt/f.length()*100);
			//
		}
		br.close();
		bw.close();
		//		
		inProgress = false;
		//
	}
	
	public static void MORSEdec(String input, String output) throws IOException {		
		//
		inProgress = true;
		cancel = false;
		progress = 0;
		cnt = 0;
		//
		File f = new File(input);
		FileReader fr = new FileReader(f);
		FileWriter fw = new FileWriter(output);
		BufferedReader br = new BufferedReader(fr);
		BufferedWriter bw = new BufferedWriter(fw);
		
		int i;
		String str = null, str2;
		while((i=br.read())!=-1 && !cancel) {
			if(i=='\n' || i=='\r')
				bw.write(i);
			else{				
				if(i!=MORSE_CHAR_DELIMITER)
					str = (char)i+"";
				else
					str = "";
				if(i==MORSE_WORD_DELIMITER)
					bw.write(" ");
				else{
					while((i=br.read())!=-1 && i!=MORSE_CHAR_DELIMITER && str.length()<100){ //<100 because can fail on memmory because if you get < and MBs of next characters
						if(i==-1)
							throw new IOException("Error, there is no MORSE code:'"+str+"'");
						if(i!=MORSE_CHAR_DELIMITER)
							str += ((char)i+"").toUpperCase();
					}
					str2 = MORSE_2_STRING_MAP.get(str);
					if(str2!=null)
						bw.write(str2);
					else
						throw new IOException("Error, there is no MORSE code:'"+str+"'");
				}							
			}
			//
			if(++cnt%10000==1)
				progress = (int)((double)cnt/f.length()*100);
			//
		}
		br.close();
		bw.close();
		//		
		inProgress = false;
		//
	}
	
	public static void main(String[] args) throws Exception {	
		/*
		String str = Normalizer
				.normalize("ýťčŤŘ", Normalizer.Form.NFD)
				.replaceAll("[^\\p{ASCII}]", "");
		
		System.out.println(str);
		System.out.println((int)'ý');
		asi urob mapu
		if(1==1)
			return;
		*/
		/*
		long  size = 35000;
		size *= 1/(MAX_DATA_IN_IMAGE_RATIO-RANDOM.nextDouble()*MIN_DATA_IN_IMAGE_RATIO);
		
		int height = (int)Math.ceil(Math.sqrt(size/WIDTH_HEIGHT_RATIO));  
		int width = (int)Math.ceil(height*WIDTH_HEIGHT_RATIO);
		
		System.out.println(size+"="+width+"*"+height);
		
		int shade = -MAX_SHADE+RANDOM.nextInt(MAX_SHADE*2);
		
		BufferedImage bi = CREATE_IMAGE(width, height, LAKE_WATER_RGB[0]+shade, LAKE_WATER_RGB[1]+shade, LAKE_WATER_RGB[2]+shade, null);
		try {
            ImageIO.write(bi,  "png", new File(LAKES[RANDOM.nextInt(LAKES.length)]+".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

		System.out.println(String.format("%07d", 55));

		System.out.print(new Date(System.currentTimeMillis()));
		FileInputStream fis = new FileInputStream(new File("data"));
		BufferedInputStream bis = new BufferedInputStream(fis);
		bis.re
		while(raf.read()!=-1);
		raf.close();
		System.out.print(new Date(System.currentTimeMillis()));
		
		if(1==1)
			return;
*/		
		int bitCount;
		long seed = DEFAULT_SEED;
		
		//System.out.println("P001,P002,P003,P004,P005,".substring(0,-1));
                /*
                bitCount = ENCODE_FILE_TO_IMAGE("c:/Users/4900063/WORK/Crypto/FileEncoder/data.dat", 
                									"c:/Users/4900063/WORK/Crypto/FileEncoder/5000x5000.jpg", 
                									"c:/Users/4900063/WORK/Crypto/FileEncoder/output.bmp", 
                									seed);
                System.out.println("Pocet bitov: "+bitCount);

                
                DECODE_FILE_FROM_IMAGE("c:/Users/4900063/WORK/Crypto/FileEncoder/output.bmp","c:/", seed);
        if(1==1)return;
		*/
		if(args.length>0) {
			if(args[0].toLowerCase().equals("-gui")) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							Frame frame = new Frame();
							frame.frame = frame;
							frame.setVisible(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
			//steganography, encode data into image
			if(args[0].toLowerCase().equals("-steg")) {
				if((args.length>=4) 											//basic 4,5
				   ||(args.length>=3 && args[1].toLowerCase().equals("-d")) 	//decoding 
				   ||(args.length==2)										 	//generating random images with default seed
				   ||(args.length==3 && !args[1].toLowerCase().equals("-d")) 	//generating random images with some seed
						){					
					if(args[1].toLowerCase().equals("-d")) {
						String path = "";						
						if(args.length>=4){
							try{
								seed = Long.parseLong(args[3]);
							}catch(Exception e){
								path = args[3];
							}
						}
						if(args.length>=5){
							seed = Long.parseLong(args[4]);
							path = args[3];
						}
						if(args[2].startsWith(".")){							
							File dir = new File("*"+args[2]);
							dir = new File(dir.getAbsolutePath()).getParentFile();
							File[] files = dir.listFiles();							
							for (int i = 0; files!=null && i < files.length; i++) 
								if(files[i].getName().endsWith(args[2])){
									System.out.println("Decoding File: "+files[i].getName());
									DECODE_FILE_FROM_IMAGE(files[i].getName(), path, new Random(seed));
								}
						}else
							DECODE_FILE_FROM_IMAGE(args[2], path, new Random(seed));						
					}else{
						if(args.length==2){							
							bitCount = ENCODE_FILE_TO_IMAGE(args[1], "-", "-", seed);
						}else{
							if(args.length==3){
								seed = Long.parseLong(args[2]);
								bitCount = ENCODE_FILE_TO_IMAGE(args[1], "-", "-", seed);
							}else{
								if(args.length>=5)
									seed = Long.parseLong(args[4]);
								bitCount = ENCODE_FILE_TO_IMAGE(args[1], args[2], args[3], seed);
							}
						}						
					}															
				}else {					
					System.out.println("Program Arguments");
					System.out.println("1 - input data file - mandatory (file to be encoded)");
					System.out.println("2 - input image file - mandatory (medium use for encoding, type character '-' for random generated)");
					System.out.println("3 - output image file - mandatory (output image with encoded data, use loseless img format for output bmp/png, etc..., type '-' for random generated)");
					System.out.println("4 - random seed(long), something like key, default "+DEFAULT_SEED);					
					System.out.println("");
					System.out.println("it encodes into image also name of data file");
					System.out.println("encode optional datafile into image using simple encryption(One Time Pad using seed) and no compression starting with less significant RGB bits up to all RGB bits according file size");
					System.out.println("this steganographic method use random ordered pixels in image base on random seed");
					
					System.out.println("examples");
					System.out.println("-steg data.zip image.jpg outImage.png	- generate file outImage.png like image.jpg with encoded data.zip");
					System.out.println("-steg data.zip image.jpg outImage.png 12345	- generate file outImage.png like image.jpg with encoded data.zip with seed(key) 12345");
					System.out.println("");
					System.out.println("use -d for decode data from image");
					System.out.println("1 - input image file - mandatory (file where data are encoded)");
					System.out.println("2 - output directory path (where to copy data file, default actual directory)");
					System.out.println("3 - random seed(long), something like key, default "+DEFAULT_SEED);					
					System.out.println("examples");
					System.out.println("-steg -d image.png - decode data from image into actual directory using seed(key) "+DEFAULT_SEED);					
					System.out.println("-steg -d image.png c:/data/ 12345  - decode data from image into directory c:/data/ using seed(key) 12345");
					System.out.println("-steg -d .png - decode data from all *.png images into actual directory using seed(key) "+DEFAULT_SEED);
				}
			}
			//file generation by secure random bytes
			if(args[0].toLowerCase().equals("-sgen")) {
				int threadCount = 1;
				if(args.length>=3) {
					if(args.length>=4)
						threadCount = Integer.parseInt(args[3]);
					GEN_SECURE_RANDOM_DATA_FILE(args[1], Integer.parseInt(args[2]), threadCount);					
				}else {					
					System.out.println("Program Arguments");
					System.out.println("1 - input file - mandatory");
					System.out.println("2 - file size(long) - mandatory");
					System.out.println("3 - thread count, you can chooose how many threads will generate file, default 1, max 1024");
					System.out.println("");
					System.out.println("generate binary file with secure random data of selected size, generating secure dat is much more slower, you can speed up it by using more threads");
					System.out.println("examples");
					System.out.println("-sgen output.bin 128 		- generate file output.bin of size 128 using 1 thread");
					System.out.println("-sgen output.bin 128 5		- generate file output.bin of size 128 using 5 thread");
				}
			}
			//FILE FREQUENCY
			if(args[0].toLowerCase().equals("-freq")) {
				//additional two arguments needed 
				if(args.length>=3) {
					GET_FILE_FREQUENCY(args[1], System.out, Integer.parseInt(args[2]));
				}else {					
					System.out.println("Program Arguments");
					System.out.println("1 - input file - mandatory");
					System.out.println("2 - bit number of tupple for analyzing");					
					System.out.println("");
					System.out.println("Counted frequency of n-bit tuple occured in binary file");
					System.out.println("The result is Number of all occured tuples, total entropy, and frequency of all occured tuples ordered by the highes one");					
				}
			}
			//ONE TIME PAD
			if(args[0].toLowerCase().equals("-enc")) {
				//additional two arguments needed 
				if(args.length>=3) {					
					if(args.length>=4)
						if(args.length>=5 && args[3].toLowerCase().equals("-key")) {
							ONE_TIME_PAD_FILE_KEY(args[1], args[2], args[4]);
							return;
						}else
							seed = Long.parseLong(args[3]);
					//long time = System.currentTimeMillis();
					ONE_TIME_PAD(args[1], args[2], new Random(seed));					
					//System.out.println(System.currentTimeMillis()-time+" milis took process");
				}else {					
					System.out.println("Program Arguments");
					System.out.println("1 - input file - mandatory");
					System.out.println("2 - output file - mandatory, using same file and will be overwritten directly");
					System.out.println("3 - random seed(long), something like key, default "+DEFAULT_SEED);
					System.out.println("");
					System.out.println("Encryption is easy One Time Pad - like cryptosystem using pseudorandom numbers");
					System.out.println("The aim is to fast and easy encrypt/decrypt file (text/binary) for purpose to pass files through mail attachment control and such systems which control type of files rather to use it for security purpose (because it use Java pseudorandom number, which are not secure for encryption)");
					System.out.println("Two time encryption return the same input (using the same key)");
					System.out.println("-------------------------------------------------------------");
					System.out.println("For security encryption use key file generated by -sgen (use same length as input file)");					
					System.out.println("For encryption by key file use command");
					System.out.println("-enc input.file output.file -key key.file");
				}
			}
			//Morse encoding/decoding
			if(args[0].toLowerCase().equals("-morse")) {
				if(args.length>=3) {
					if(args.length>=4 && args[1].toLowerCase().equals("-d")) {
						//decoding
						MORSEdec(args[2], args[3]);
					}else{
						//encoding
						MORSEenc(args[1], args[2]);
					}
				}else {					
					System.out.println("Program Arguments");
					System.out.println("1 - input file - mandatory");
					System.out.println("2 - output file - mandatory");		
					System.out.println("");
					System.out.println("encode input file (text) to output text file morse encoded");
					System.out.println("use -d for for decoding input morse text file to text file");
					System.out.println("examples");
					System.out.println("-morse input.txt b2.txt");
					System.out.println("-morse -d b2.txt input.txt");
				}
			}
			
			//base2 encoding/decoding
			if(args[0].toLowerCase().equals("-base2") || args[0].toLowerCase().equals("-b2")) {
				if(args.length>=3) {
					if(args.length>=4 && args[1].toLowerCase().equals("-d")) {
						//decoding
						BASE2dec(args[2], args[3]);
					}else{
						//encoding
						BASE2enc(args[1], args[2]);
					}
				}else {					
					System.out.println("Program Arguments");
					System.out.println("1 - input file - mandatory");
					System.out.println("2 - output file - mandatory");		
					System.out.println("");
					System.out.println("encode input file (text/binary) to output text file base2 encoded");
					System.out.println("use -d for for decoding input base16 file to binary/text file");
					System.out.println("examples");
					System.out.println("-base2 input.bin b2.txt");
					System.out.println("-base2 -d b2.txt input.bin");
				}
			}		
			//base16 encoding/decoding
			if(args[0].toLowerCase().equals("-base16") || args[0].toLowerCase().equals("-b16")) {
				if(args.length>=3) {
					if(args.length>=4 && args[1].toLowerCase().equals("-d")) {
						//decoding
						BASE16dec(args[2], args[3]);
					}else{
						//encoding
						BASE16enc(args[1], args[2]);
					}
				}else {					
					System.out.println("Program Arguments");
					System.out.println("1 - input file - mandatory");
					System.out.println("2 - output file - mandatory");		
					System.out.println("");
					System.out.println("encode input file (text/binary) to output text file base16 encoded");
					System.out.println("use -d for for decoding input base16 file to binary/text file");
					System.out.println("examples");
					System.out.println("-base16 input.bin b16.txt");
					System.out.println("-base16 -d b16.txt input.bin");
				}
			}		
			//base64 encoding/decoding
			if(args[0].toLowerCase().equals("-base64") || args[0].toLowerCase().equals("-b64")) {
				if(args.length>=3) {
					if(args.length>=4 && args[1].toLowerCase().equals("-d")) {
						//decoding
						BASE64dec(args[2], args[3]);
					}else{
						//encoding
						BASE64enc(args[1], args[2]);
					}
				}else {					
					System.out.println("Program Arguments");
					System.out.println("1 - input file - mandatory");
					System.out.println("2 - output file - mandatory");		
					System.out.println("");
					System.out.println("encode input file (text/binary) to output text file base64 encoded");
					System.out.println("use -d for for decoding input base64 file to binary/text file");
					System.out.println("examples");
					System.out.println("-base64 input.bin b64.txt");
					System.out.println("-base64 -d b64.txt input.bin");
				}
			}
			//file generation
			if(args[0].toLowerCase().equals("-gen")) {
				if(args.length>=3) {
					if(args.length>=4)
						seed = Long.parseLong(args[3]);
					GEN_RANDOM_DATA_FILE(args[1], Long.parseLong(args[2]), new Random(seed));
				}else {					
					System.out.println("Program Arguments");
					System.out.println("1 - input file - mandatory");
					System.out.println("2 - file size(long) - mandatory");
					System.out.println("3 - random seed(long), something like key, default "+DEFAULT_SEED);
					System.out.println("");
					System.out.println("generate binary file with random data of selected size");
					System.out.println("examples");
					System.out.println("-gen output.bin 1024 		- generate file output.bin of size 1024 with default seed "+DEFAULT_SEED);
					System.out.println("-gen output.bin 1024 13245	- generate file output.bin of size 1024 with seed 12345 (same seed generate same data)");
				}
			}		
			//file generation
			if(args[0].toLowerCase().equals("-del")) {
				if(args.length>=2) {
					if(args.length>=3)
						seed = Long.parseLong(args[2]);
					SECURE_DELETE(args[1], new Random(seed));
				}else {					
					System.out.println("Program Arguments");
					System.out.println("1 - input file/folder - mandatory");					
					System.out.println("2 - random seed(long), default "+DEFAULT_SEED);
					System.out.println("");
					System.out.println("secure delete mean rewriting the data instead of common delete in system, when only file is marked as delete and the data stay unchanged");
					System.out.println("function rewrites content and name for file or folder (recursively to all subfolders and files) to random data");
					System.out.println("used Java random, for more secure it is recommended to run function more times with different seed");
					System.out.println("examples");
					System.out.println("-del fileTo.del		- delete file fileTo.del with default seed "+DEFAULT_SEED);
					System.out.println("-del fileTo.del	12345 	- delete file fileTo.del using seed 12345");
				}
			}
		}else {
			//no arguments or -h
			System.out.println("*********************************************************************************************************************");			
			System.out.println("Program offer next functions:");
			System.out.println("-enc 	- Encryption (One Time Pad)");
			System.out.println("-steg 	- Encoding data into image (Steganography)");
			System.out.println("-base2  - base2 encoding");			
			System.out.println("-base16 - base16 encoding");			
			System.out.println("-base64 - base64 encoding");
			System.out.println("-morse 	- morse encoding");
			System.out.println("-gen 	- Random data generating");
			System.out.println("-sgen 	- Secure random data generating");
			System.out.println("-freq 	- Frequency analysis of binary file");
			System.out.println("-del 	- Secure delete for files and folders (rewrite data inside files and names to random data)");
			System.out.println("\nrun with argument to see further details");
			System.out.println("");
			System.out.println("-gui 	- run Graphical User Interface program with all mentioned function");
		}
		
		
	}

	/*random generating lakes water constants*/
	public static final Random RANDOM = PRODUCTION?new Random():new Random(DEFAULT_SEED);
	public static final long MAX_FILE_SIZE = 15_000_000; //max size 15MB, if more file will be splitted to 15MB parts
	public static final long MIN_FILE_SIZE = 15_000; //min file size 15KB, to prevent very small images
	public static final int[] STAR_SURFACE_RGB = {255, 191, 0};
	public static final int[] LAKE_WATER_RGB = {67, 111, 166};
	public static final int[] MOON_ARTEFACT_RGB = {166, 166, 166};
	public static final int MAX_SHADE = 50;
	public static final double WIDTH_HEIGHT_RATIO = 16/9d;
	public static final double MAX_DATA_IN_IMAGE_RATIO = .9;//0.6;
	public static final double MIN_DATA_IN_IMAGE_RATIO = .6;//0.3;
	
	//moon artefacts sea, lakes, craters etc,,, https://en.wikipedia.org/wiki/List_of_lunar_features
	public static final String[] MOON_ARTEFACTS = {
			"Mare_Anguis",
			"Mare_Australe",
			"Mare_Cognitum",
			"Mare_Crisium",
			"Mare_Fecunditatis",
			"Mare_Frigoris",
			"Mare_Humboldtianum",
			"Mare_Humorum",
			"Mare_Imbrium",
			"Mare_Ingenii",
			"Mare_Insularum",
			"Mare_Marginis",
			"Mare_Moscoviense",
			"Mare_Nectaris",
			"Mare_Nubium",
			"Mare_Orientale",
			"Mare_Serenitatis",
			"Mare_Smythii",
			"Mare_Spumans",
			"Mare_Tranquillitatis",
			"Mare_Undarum",
			"Mare_Vaporum",
			"Oceanus_Procellarum",
			"Lacus_Aestatis",
			"Lacus_Autumni",
			"Lacus_Bonitatis",
			"Lacus_Doloris",
			"Lacus_Excellentiae",
			"Lacus_Felicitatis",
			"Lacus_Gaudii",
			"Lacus_Hiemalis",
			"Lacus_Lenitatis",
			"Lacus_Luxuriae",
			"Lacus_Mortis",
			"Lacus_Oblivionis",
			"Lacus_Odii",
			"Lacus_Perseverantiae",
			"Lacus_Solitudinis",
			"Lacus_Somniorum",
			"Lacus_Spei",
			"Lacus_Temporis",
			"Lacus_Timoris",
			"Lacus_Veris",
			"Palus_Epidemiarum",
			"Palus_Putredinis",
			"Palus_Somni",
			"Sinus_Aestuum",
			"Sinus_Amoris",
			"Sinus_Asperitatis",
			"Sinus_Concordiae",
			"Sinus_Fidei",
			"Sinus_Honoris",
			"Sinus_Iridum",
			"Sinus_Lunicus",
			"Sinus_Medii",
			"Sinus_Roris",
			"Sinus_Successus",
			"Catena_Abulfeda",
			"Catena_Artamonov",
			"Catena_Brigitte",
			"Catena_Davy",
			"Catena_Dziewulski",
			"Catena_Gregory",
			"Catena_Humboldt",
			"Catena_Krafft",
			"Catena_Kurchatov",
			"Catena_Leuschner",
			"Catena_Littrow",
			"Catena_Lucretius",
			"Catena_Mendeleev",
			"Catena_Michelson",
			"Catena_Pierre",
			"Catena_Sumner",
			"Catena_Sylvester",
			"Catena_Taruntius",
			"Catena_Timocharis",
			"Catena_Yuri",
			"Vallis_Alpes",
			"Vallis_Baade",
			"Vallis_Bohr",
			"Vallis_Bouvard",
			"Vallis_Capella",
			"Vallis_Inghirami",
			"Vallis_Palitzsch",
			"Vallis_Planck",
			"Vallis_Rheita",
			"Vallis_Schrödinger",
			"Vallis_Schröteri",
			"Vallis_Snellius",
			"Mons_Agnes",
			"Mons_Ampère",
			"Mons_André",
			"Mons_Ardeshir",
			"Mons_Argaeus",
			"Mont_Blanc",
			"Mons_Bradley",
			"Mons_Delisle",
			"Mons_Dieter",
			"Mons_Dilip",
			"Mons_Esam",
			"Mons_Ganau",
			"Mons_Gruithuisen_Delta",
			"Mons_Gruithuisen_Gamma",
			"Mons_Hadley",
			"Mons_Hadley_Delta",
			"Mons_Hansteen",
			"Mons_Herodotus",
			"Mons_Huygens",
			"Mons_La_Hire",
			"Mons_Maraldi",
			"Mons_Moro",
			"Mons_Penck",
			"Mons_Pico",
			"Mons_Piton",
			"Mons_Rümker",
			"Mons_Usov",
			"Mons_Vinogradov",
			"Mons_Vitruvius",
			"Mons_Wolff",
			"Montes_Agricola",
			"Montes_Alpes",
			"Montes_Apenninus",
			"Montes_Archimedes",
			"Montes_Carpatus",
			"Montes_Caucasus",
			"Montes_Cordillera",
			"Montes_Haemus",
			"Montes_Harbinger",
			"Montes_Jura",
			"Montes_Pyrenaeus",
			"Montes_Recti",
			"Montes_Riphaeus",
			"Montes_Rook",
			"Montes_Secchi",
			"Montes_Spitzbergen",
			"Montes_Taurus",
			"Montes_Teneriffe",
			"Reiner_Gamma",
			"Dorsa_Aldrovandi",
			"Dorsa_Andrusov",
			"Dorsum_Arduino",
			"Dorsa_Argand",
			"Dorsum_Azara",
			"Dorsa_Barlow",
			"Dorsum_Bucher",
			"Dorsum_Buckland",
			"Dorsa_Burnet",
			"Dorsa_Cato",
			"Dorsum_Cayeux",
			"Dorsum_Cloos",
			"Dorsum_Cushman",
			"Dorsa_Dana",
			"Dorsa_Ewing",
			"Dorsum_Gast",
			"Dorsa_Geikie",
			"Dorsum_Grabau",
			"Dorsum_Guettard",
			"Dorsa_Harker",
			"Dorsum_Heim",
			"Dorsum_Higazy",
			"Dorsa_Lister",
			"Dorsa_Mawson",
			"Dorsum_Nicol",
			"Dorsum_Niggli",
			"Dorsum_Oppel",
			"Dorsum_Owen",
			"Dorsa_Rubey",
			"Dorsum_Scilla",
			"Dorsa_Smirnov",
			"Dorsa_Sorby",
			"Dorsa_Stille",
			"Dorsum_Termier",
			"Dorsa_Tetyaev",
			"Dorsum_Thera",
			"Dorsum_Von_Cotta",
			"Dorsa_Whiston",
			"Dorsum_Zirkel",
			"Promontorium_Agarum",
			"Promontorium_Agassiz",
			"Promontorium_Archerusia",
			"Promontorium_Deville",
			"Promontorium_Fresnel",
			"Promontorium_Heraclides",
			"Promontorium_Kelvin",
			"Promontorium_Laplace",
			"Promontorium_Taenarium",
			"Rima_Agatharchides",
			"Rima_Agricola",
			"Rimae_Alphonsus",
			"Rimae_Apollonius",
			"Rimae_Archimedes",
			"Rima_Archytas",
			"Rima_Ariadaeus",
			"Rimae_Aristarchus",
			"Rimae_Arzachel",
			"Rimae_Atlas",
			"Rima_Billy",
			"Rima_Birt",
			"Rimae_Bode",
			"Rimae_Boscovich",
			"Rima_Bradley",
			"Rima_Brayley",
			"Rima_Calippus",
			"Rima_Cardanus",
			"Rima_Carmen",
			"Rima_Cauchy",
			"Rimae_Chacornac",
			"Rima_Cleomedes",
			"Rima_Cleopatra",
			"Rima_Conon",
			"Rimae_Daniell",
			"Rimae_Darwin",
			"Rima_Dawes",
			"Rimae_de_Gasparis",
			"Rima_Delisle",
			"Rima_Diophantus",
			"Rimae_Doppelmayer",
			"Rima_Draper",
			"Rima_Euler",
			"Rima_Flammarion",
			"Rimae_Focas",
			"Rimae_Fresnel",
			"Rima_Furnerius",
			"Rima_Galilaei",
			"Rima_Gärtner",
			"Rimae_Gassendi",
			"Rima_Gay-Lussac",
			"Rima_G_Bond",
			"Rimae_Gerard",
			"Rimae_Goclenius",
			"Rimae_Grimaldi",
			"Rima_Hadley",
			"Rima_Hansteen",
			"Rima_Hesiodus",
			"Rima_Hyginus",
			"Rimae_Hypatia",
			"Rima_Jansen",
			"Rimae_Janssen",
			"Rimae_Kopff",
			"Rima_Krieger",
			"Rimae_Liebig",
			"Rimae_Littrow",
			"Rimae_Maclear",
			"Rimae_Maestlin",
			"Rima_Mairan",
			"Rima_Marcello",
			"Rima_Marius",
			"Rimae_Maupertuis",
			"Rimae_Menelaus",
			"Rimae_Mersenius",
			"Rima_Messier",
			"Rima_Milichius",
			"Rimae_Opelt",
			"Rima_Oppolzer",
			"Rimae_Palmieri",
			"Rimae_Parry",
			"Rimae_Petavius",
			"Rimae_Pettit",
			"Rimae_Pitatus",
			"Rimae_Plato",
			"Rimae_Plinius",
			"Rimae_Posidonius",
			"Rimae_Prinz",
			"Rimae_Ramsden",
			"Rima_Réaumur",
			"Rima_Reiko",
			"Rimae_Repsold",
			"Rimae_Riccioli",
			"Rimae_Ritter",
			"Rimae_Römer",
			"Rima_Rudolf",
			"Rima_Schröter",
			"Rimae_Secchi",
			"Rima_Sharp",
			"Rima_Sheepshanks",
			"Rima_Siegfried",
			"Rimae_Sirsalis",
			"Rimae_Sosigenes",
			"Rima_Suess",
			"Rimae_Sulpicius_Gallus",
			"Rima_Sung-Mei",
			"Rimae_Taruntius",
			"Rimae_Theaetetus",
			"Rima_T_Mayer",
			"Rimae_Triesnecker",
			"Rimae_Vasco_da_Gama",
			"Rima_Vladimir",
			"Rima_Wan-Yu",
			"Rima_Yangel",
			"Rima_Zahia",
			"Rimae_Zupus",
			"Rupes_Altai",
			"Rupes_Boris",
			"Rupes_Cauchy",
			"Rupes_Kelvin",
			"Rupes_Liebig",
			"Rupes_Mercator",
			"Rupes_Recta",
			"Rupes_Toscanelli",
			"Insula_Ventorum",
			"Peninsula_Fulminum",
			"Terra_Caloris",
			"Terra_Fertilitatis",
			"Terrae_Grandinis",
			"Terrae_Manna",
			"Terra_Nivium",
			"Terra_Pruinae",
			"Terra_Sanitatis",
			"Terra_Siccitatis",
			"Terra_Sterilitatis",
			"Terra_Vigoris",
			"Terra_Vitae",
			"Crater_8_Homeward",
			"Crater_Abbe",
			"Crater_Abbot",
			"Crater_Abduh",
			"Crater_Abel",
			"Crater_Abenezra",
			"Crater_Abetti",
			"Crater_Abulfeda",
			"Crater_Abul_Wáfa",
			"Crater_Acosta",
			"Crater_Adams",
			"Crater_Aepinus",
			"Crater_Agatharchides",
			"Crater_Agrippa",
			"Crater_Airy",
			"Crater_Aitken",
			"Crater_Akis",
			"Crater_Alan",
			"Crater_Al-Bakri",
			"Crater_Albategnius",
			"Crater_Albert",
			"Crater_Al-Biruni",
			"Crater_Alden",
			"Crater_Alder",
			"Crater_Aldrin",
			"Crater_Alekhin",
			"Crater_Alexander",
			"Crater_Alfraganus",
			"Crater_Alhazen",
			"Crater_Aliacensis",
			"Crater_Al-Khwarizmi",
			"Crater_Almanon",
			"Crater_Al-Marrakushi",
			"Crater_Aloha",
			"Crater_Alpetragius",
			"Crater_Alphonsus",
			"Crater_Alter",
			"Crater_Ameghino",
			"Crater_Amici",
			"Crater_Ammonius",
			"Crater_Amontons",
			"Crater_Amundsen",
			"Crater_Anaxagoras",
			"Crater_Anaximander",
			"Crater_Anaximenes",
			"Crater_Anděl",
			"Crater_Anders",
			"Crater_Anders_Earthrise",
			"Crater_Anderson",
			"Crater_Andersson",
			"Crater_Andronov",
			"Crater_Ango",
			"Crater_Angström",
			"Crater_Ann",
			"Crater_Annegrit",
			"Crater_Ansgarius",
			"Crater_Antoniadi",
			"Crater_Anuchin",
			"Crater_Anville",
			"Crater_Apianus",
			"Crater_Apollo",
			"Crater_Apollonius",
			"Crater_Appleton",
			"Crater_Arago",
			"Crater_Aratus",
			"Crater_Archimedes",
			"Crater_Archytas",
			"Crater_Argelander",
			"Crater_Ariadaeus",
			"Crater_Ariosto",
			"Crater_Aristarchus",
			"Crater_Aristillus",
			"Crater_Aristoteles",
			"Crater_Armiński",
			"Crater_Armstrong",
			"Crater_Arnold",
			"Crater_Arrhenius",
			"Crater_Artamonov",
			"Crater_Artemev",
			"Crater_Artemis",
			"Crater_Artsimovich",
			"Crater_Aryabhata",
			"Crater_Arzachel",
			"Crater_Asada",
			"Crater_Asclepi",
			"Crater_Ashbrook",
			"Crater_Aston",
			"Crater_Atlas",
			"Crater_Atwood",
			"Crater_Austen",
			"Crater_Autolycus",
			"Crater_Auwers",
			"Crater_Auzout",
			"Crater_Avery",
			"Crater_Avicenna",
			"Crater_Avogadro",
			"Crater_Azophi",
			"Crater_Baade",
			"Crater_Babakin",
			"Crater_Babbage",
			"Crater_Babcock",
			"Crater_Back",
			"Crater_Backlund",
			"Crater_Baco",
			"Crater_Baillaud",
			"Crater_Bailly",
			"Crater_Baily",
			"Crater_Balandin",
			"Crater_Balboa",
			"Crater_Baldet",
			"Crater_Ball",
			"Crater_Ballet",
			"Crater_Balmer",
			"Crater_Balzac",
			"Crater_Banachiewicz",
			"Crater_Bancroft",
			"Crater_Bandfield",
			"Crater_Banting",
			"Crater_Barbier",
			"Crater_Barkla",
			"Crater_Barnard",
			"Crater_Barocius",
			"Crater_Barringer",
			"Crater_Barrow",
			"Crater_Bartels",
			"Crater_Baudelaire",
			"Crater_Bawa",
			"Crater_Bayer",
			"Crater_Beals",
			"Crater_Beaumont",
			"Crater_Becquerel",
			"Crater_Bečvář",
			"Crater_Beer",
			"Crater_Behaim",
			"Crater_Beijerinck",
			"Crater_Beketov",
			"Crater_Béla",
			"Crater_Belkovich",
			"Crater_Bell",
			"Crater_Bellinsgauzen",
			"Crater_Bellot",
			"Crater_Belopolskiy",
			"Crater_Belyaev",
			"Crater_Benedict",
			"Crater_Bergman",
			"Crater_Bergstrand",
			"Crater_Berkner",
			"Crater_Berlage",
			"Crater_Bernini",
			"Crater_Bernoulli",
			"Crater_Berosus",
			"Crater_Berzelius",
			"Crater_Bessarion",
			"Crater_Bessel",
			"Crater_Bettinus",
			"Crater_Bhabha",
			"Crater_Bianchini",
			"Crater_Biela",
			"Crater_Bilharz",
			"Crater_Billy",
			"Crater_Bingham",
			"Crater_Biot",
			"Crater_Birkeland",
			"Crater_Birkhoff",
			"Crater_Birmingham",
			"Crater_Birt",
			"Crater_Bi_Sheng",
			"Crater_Bjerknes",
			"Crater_Black",
			"Crater_Blackett",
			"Crater_Blagg",
			"Crater_Blancanus",
			"Crater_Blanchard",
			"Crater_Blanchinus",
			"Crater_Blazhko",
			"Crater_Bliss",
			"Crater_Bobillier",
			"Crater_Bobone",
			"Crater_Bode",
			"Crater_Boethius",
			"Crater_Boguslawsky",
			"Crater_Bohnenberger",
			"Crater_Bohr",
			"Crater_Bok",
			"Crater_Boltzmann",
			"Crater_Bolyai",
			"Crater_Bombelli",
			"Crater_Bondarenko",
			"Crater_Bonpland",
			"Crater_Boole",
			"Crater_Borda",
			"Crater_Borel",
			"Crater_Boris",
			"Crater_Borman",
			"Crater_Born",
			"Crater_Borya",
			"Crater_Bosch",
			"Crater_Boscovich",
			"Crater_Bose",
			"Crater_Boss",
			"Crater_Bouguer",
			"Crater_Boussingault",
			"Crater_Bowditch",
			"Crater_Bowen",
			"Crater_Boyle",
			"Crater_Brackett",
			"Crater_Bragg",
			"Crater_Brashear",
			"Crater_Braude",
			"Crater_Brayley",
			"Crater_Bredikhin",
			"Crater_Breislak",
			"Crater_Brenner",
			"Crater_Brewster",
			"Crater_Brianchon",
			"Crater_Bridgman",
			"Crater_Briggs",
			"Crater_Brill",
			"Crater_Brisbane",
			"Crater_Bronk",
			"Crater_Brouwer",
			"Crater_Brown",
			"Crater_Bruce",
			"Crater_Brunner",
			"Crater_Buch",
			"Crater_Buffon",
			"Crater_Buisson",
			"Crater_Bullialdus",
			"Crater_Bunsen",
			"Crater_Burbidge",
			"Crater_Burckhardt",
			"Crater_Bürg",
			"Crater_Burnham",
			"Crater_Büsching",
			"Crater_Butlerov",
			"Crater_Buys-Ballot",
			"Crater_Byrd",
			"Crater_Byrgius",
			"Crater_Cabannes",
			"Crater_Cabeus",
			"Crater_Cailleux",
			"Crater_Cai_Lun",
			"Crater_Cajal",
			"Crater_Cajori",
			"Crater_Calippus",
			"Crater_Cameron",
			"Crater_Camoens",
			"Crater_Campanus",
			"Crater_Campbell",
			"Crater_Cannizzaro",
			"Crater_Cannon",
			"Crater_Cantor",
			"Crater_Capella",
			"Crater_Capuanus",
			"Crater_Cardanus",
			"Crater_Carlini",
			"Crater_Carlos",
			"Crater_Carmichael",
			"Crater_Carnot",
			"Crater_Carol",
			"Crater_Carpenter",
			"Crater_Carrel",
			"Crater_Carrillo",
			"Crater_Carrington",
			"Crater_Cartan",
			"Crater_Carver",
			"Crater_Casatus",
			"Crater_Cassegrain",
			"Crater_Cassini",
			"Crater_Catalán",
			"Crater_Catharina",
			"Crater_Cauchy",
			"Crater_Cavalerius",
			"Crater_Cavendish",
			"Crater_Caventou",
			"Crater_Cayley",
			"Crater_Cellini",
			"Crater_Celsius",
			"Crater_Censorinus",
			"Crater_Cepheus",
			"Crater_Cervantes",
			"Crater_Chacornac",
			"Crater_Chadwick",
			"Crater_Chaffee",
			"Crater_Challis",
			"Crater_Chalonge",
			"Crater_Chamberlin",
			"Crater_Champollion",
			"Crater_Chandler",
			"Crater_Chang_Heng",
			"Crater_Chang-Ngo",
			"Crater_Chant",
			"Crater_Chaplygin",
			"Crater_Chapman",
			"Crater_Chappe",
			"Crater_Chappell",
			"Crater_Charles",
			"Crater_Charlier",
			"Crater_Chaucer",
			"Crater_Chauvenet",
			"Crater_Chawla",
			"Crater_Chebyshev",
			"Crater_Chekov",
			"Crater_Chenier",
			"Crater_Chernyshev",
			"Crater_C_Herschel",
			"Crater_Chevallier",
			"Crater_Chien-Shiung_Wu",
			"Crater_Ching-Te",
			"Crater_Chladni",
			"Crater_Chrétien",
			"Crater_Cichus",
			"Crater_Clairaut",
			"Crater_Clark",
			"Crater_Clausius",
			"Crater_Clavius",
			"Crater_Cleomedes",
			"Crater_Cleostratus",
			"Crater_Clerke",
			"Crater_C_Mayer",
			"Crater_Coblentz",
			"Crater_Cockcroft",
			"Crater_Collins",
			"Crater_Colombo",
			"Crater_Compton",
			"Crater_Comrie",
			"Crater_Comstock",
			"Crater_Condon",
			"Crater_Condorcet",
			"Crater_Congreve",
			"Crater_Conon",
			"Crater_Cook",
			"Crater_Cooper",
			"Crater_Copernicus",
			"Crater_Cori",
			"Crater_Coriolis",
			"Crater_Corneille",
			"Crater_Couder",
			"Crater_Coulomb",
			"Crater_Courtney",
			"Crater_Cremona",
			"Crater_Crile",
			"Crater_Crocco",
			"Crater_Crommelin",
			"Crater_Crookes",
			"Crater_Crozier",
			"Crater_Crüger",
			"Crater_Ctesibius",
			"Crater_Curie",
			"Crater_Curtis",
			"Crater_Curtius",
			"Crater_Cusanus",
			"Crater_Cuvier",
			"Crater_Cyrano",
			"Crater_Cyrillus",
			"Crater_Cysatus",
			"Crater_Daedalus",
			"Crater_Dag",
			"Crater_Daguerre",
			"Crater_Dale",
			"Crater_dAlembert",
			"Crater_Dalton",
			"Crater_Daly",
			"Crater_Damoiseau",
			"Crater_Daniell",
			"Crater_Danjon",
			"Crater_Dante",
			"Crater_Dario",
			"Crater_Darney",
			"Crater_DArrest",
			"Crater_DArsonval",
			"Crater_Darwin",
			"Crater_Das",
			"Crater_Daubrée",
			"Crater_da_Vinci",
			"Crater_Davisson",
			"Crater_Davy",
			"Crater_Dawa",
			"Crater_Dawes",
			"Crater_Dawson",
			"Crater_D_Brown",
			"Crater_Debes",
			"Crater_Debus",
			"Crater_Debye",
			"Crater_Dechen",
			"Crater_Defoe",
			"Crater_De_Forest",
			"Crater_de_Gasparis",
			"Crater_de_Gerlache",
			"Crater_Delambre",
			"Crater_De_La_Rue",
			"Crater_Delaunay",
			"Crater_Delia",
			"Crater_Delisle",
			"Crater_Dellinger",
			"Crater_Delmotte",
			"Crater_Delporte",
			"Crater_Deluc",
			"Crater_Dembowski",
			"Crater_Democritus",
			"Crater_Demonax",
			"Crater_De_Moraes",
			"Crater_De_Morgan",
			"Crater_Denning",
			"Crater_De_Roy",
			"Crater_Desargues",
			"Crater_Descartes",
			"Crater_Deseilligny",
			"Crater_De_Sitter",
			"Crater_Deslandres",
			"Crater_Deutsch",
			"Crater_De_Vico",
			"Crater_De_Vries",
			"Crater_Dewar",
			"Crater_Diana",
			"Crater_Diderot",
			"Crater_Dionysius",
			"Crater_Diophantus",
			"Crater_Dirichlet",
			"Crater_Dobrovolskiy",
			"Crater_Doerfel",
			"Crater_Dollond",
			"Crater_Donati",
			"Crater_Donna",
			"Crater_Donner",
			"Crater_Doppelmayer",
			"Crater_Doppler",
			"Crater_Douglass",
			"Crater_Dove",
			"Crater_Doyle",
			"Crater_Draper",
			"Crater_Drebbel",
			"Crater_Dreyer",
			"Crater_Drude",
			"Crater_Dryden",
			"Crater_Drygalski",
			"Crater_Dubyago",
			"Crater_Dufay",
			"Crater_Dugan",
			"Crater_Dumas",
			"Crater_Dunér",
			"Crater_Dunthorne",
			"Crater_Dyson",
			"Crater_Dziewulski",
			"Crater_Easley",
			"Crater_Eckert",
			"Crater_Eddington",
			"Crater_Edison",
			"Crater_Edith",
			"Crater_Egede",
			"Crater_Ehrlich",
			"Crater_Eichstadt",
			"Crater_Eijkman",
			"Crater_Eimmart",
			"Crater_Einstein",
			"Crater_Einthoven",
			"Crater_Elger",
			"Crater_El_Greco",
			"Crater_Ellerman",
			"Crater_Ellison",
			"Crater_Elmer",
			"Crater_Elvey",
			"Crater_Emden",
			"Crater_Encke",
			"Crater_Endymion",
			"Crater_Engelgardt",
			"Crater_Eötvös",
			"Crater_Epigenes",
			"Crater_Epimenides",
			"Crater_Eppinger",
			"Crater_Eratosthenes",
			"Crater_Erlanger",
			"Crater_Erro",
			"Crater_Esclangon",
			"Crater_Esnault-Pelterie",
			"Crater_Espin",
			"Crater_Euclides",
			"Crater_Euctemon",
			"Crater_Eudoxus",
			"Crater_Euler",
			"Crater_Evans",
			"Crater_Evdokimov",
			"Crater_Evershed",
			"Crater_Ewen",
			"Crater_Fabbroni",
			"Crater_Fabricius",
			"Crater_Fabry",
			"Crater_Fahrenheit",
			"Crater_Fairouz",
			"Crater_Faraday",
			"Crater_Faustini",
			"Crater_Fauth",
			"Crater_Faye",
			"Crater_Fechner",
			"Crater_Fedorov",
			"Crater_Felix",
			"Crater_Fényi",
			"Crater_Feoktistov",
			"Crater_Fermat",
			"Crater_Fermi",
			"Crater_Fernelius",
			"Crater_Fersman",
			"Crater_Fesenkov",
			"Crater_Feuillée",
			"Crater_Fibiger",
			"Crater_Finsch",
			"Crater_Finsen",
			"Crater_Firdausi",
			"Crater_Firmicus",
			"Crater_Firsov",
			"Crater_Fischer",
			"Crater_Fitzgerald",
			"Crater_Fizeau",
			"Crater_Flammarion",
			"Crater_Flamsteed",
			"Crater_Fleming",
			"Crater_Florensky",
			"Crater_Florey",
			"Crater_Floss",
			"Crater_Focas",
			"Crater_Fontana",
			"Crater_Fontenelle",
			"Crater_Foster",
			"Crater_Foucault",
			"Crater_Fourier",
			"Crater_Fowler",
			"Crater_Fox",
			"Crater_Fracastorius",
			"Crater_Fra_Mauro",
			"Crater_Franck",
			"Crater_Franklin",
			"Crater_Franz",
			"Crater_Fraunhofer",
			"Crater_Fredholm",
			"Crater_Freud",
			"Crater_Freundlich",
			"Crater_Fridman",
			"Crater_Froelich",
			"Crater_Frost",
			"Crater_Fryxell",
			"Crater_Furnerius",
			"Crater_Gadomski",
			"Crater_Gagarin",
			"Crater_Galen",
			"Crater_Galilaei",
			"Crater_Galimov",
			"Crater_Galle",
			"Crater_Galois",
			"Crater_Galvani",
			"Crater_Gambart",
			"Crater_Gamow",
			"Crater_Ganskiy",
			"Crater_Ganswindt",
			"Crater_Garavito",
			"Crater_Gardner",
			"Crater_Gärtner",
			"Crater_Gassendi",
			"Crater_Gaston",
			"Crater_Gaudibert",
			"Crater_Gauricus",
			"Crater_Gauss",
			"Crater_Gavrilov",
			"Crater_Gay-Lussac",
			"Crater_G_Bond",
			"Crater_Geber",
			"Crater_Geiger",
			"Crater_Geissler",
			"Crater_Geminus",
			"Crater_Gemma_Frisius",
			"Crater_Gena",
			"Crater_Gerard",
			"Crater_Gerasimovich",
			"Crater_Gernsback",
			"Crater_Gibbs",
			"Crater_Gilbert",
			"Crater_Gill",
			"Crater_Ginzel",
			"Crater_Gioja",
			"Crater_Giordano_Bruno",
			"Crater_Glaisher",
			"Crater_Glauber",
			"Crater_Glazenap",
			"Crater_Glushko",
			"Crater_Goclenius",
			"Crater_Goddard",
			"Crater_Godin",
			"Crater_Goldschmidt",
			"Crater_Golgi",
			"Crater_Golitsyn",
			"Crater_Golovin",
			"Crater_Goodacre",
			"Crater_Gore",
			"Crater_Gould",
			"Crater_Grace",
			"Crater_Grachev",
			"Crater_Graff",
			"Crater_Grave",
			"Crater_Greaves",
			"Crater_Green",
			"Crater_Gregory",
			"Crater_Grigg",
			"Crater_Grignard",
			"Crater_Grimaldi",
			"Crater_Grimm",
			"Crater_Grissom",
			"Crater_Grotrian",
			"Crater_Grove",
			"Crater_Gruemberger",
			"Crater_Gruithuisen",
			"Crater_Guericke",
			"Crater_Guest",
			"Crater_Guillaume",
			"Crater_Gullstrand",
			"Crater_Gum",
			"Crater_Gutenberg",
			"Crater_Guthnick",
			"Crater_Guyot",
			"Crater_Gyldén",
			"Crater_Haber",
			"Crater_Hadley",
			"Crater_Hagecius",
			"Crater_Hagen",
			"Crater_Hahn",
			"Crater_Haidinger",
			"Crater_Hainzel",
			"Crater_Haldane",
			"Crater_Hale",
			"Crater_Hall",
			"Crater_Halley",
			"Crater_Hamilton",
			"Crater_Hanno",
			"Crater_Hansen",
			"Crater_Hansteen",
			"Crater_Harden",
			"Crater_Harding",
			"Crater_Haret",
			"Crater_Hargreaves",
			"Crater_Harkhebi",
			"Crater_Harlan",
			"Crater_Harold",
			"Crater_Harpalus",
			"Crater_Harriot",
			"Crater_Hartmann",
			"Crater_Hartwig",
			"Crater_Harvey",
			"Crater_Hase",
			"Crater_Haskin",
			"Crater_Hatanaka",
			"Crater_Hausen",
			"Crater_Hawke",
			"Crater_Haworth",
			"Crater_Hayford",
			"Crater_Hayn",
			"Crater_Healy",
			"Crater_Heaviside",
			"Crater_Hecataeus",
			"Crater_Hédervári",
			"Crater_Hedin",
			"Crater_Hegu",
			"Crater_Heine",
			"Crater_Heinrich",
			"Crater_Heinsius",
			"Crater_Heis",
			"Crater_Helberg",
			"Crater_Helicon",
			"Crater_Hell",
			"Crater_Helmert",
			"Crater_Helmholtz",
			"Crater_Henderson",
			"Crater_Hendrix",
			"Crater_Henry",
			"Crater_Henry_Frères",
			"Crater_Henson",
			"Crater_Henyey",
			"Crater_Heraclitus",
			"Crater_Hercules",
			"Crater_Herigonius",
			"Crater_Hermann",
			"Crater_Hermite",
			"Crater_Herodotus",
			"Crater_Heron",
			"Crater_Herschel",
			"Crater_Hertz",
			"Crater_Hertzsprung",
			"Crater_Hesiodus",
			"Crater_Hess",
			"Crater_Hevelius",
			"Crater_Hevesy",
			"Crater_Heymans",
			"Crater_Heyrovsky",
			"Crater_H_G_Wells",
			"Crater_Hilbert",
			"Crater_Hildegard",
			"Crater_Hill",
			"Crater_Hind",
			"Crater_Hinshelwood",
			"Crater_Hippalus",
			"Crater_Hipparchus",
			"Crater_Hippocrates",
			"Crater_Hirayama",
			"Crater_Hoffmeister",
			"Crater_Hogg",
			"Crater_Hohmann",
			"Crater_Holden",
			"Crater_Holetschek",
			"Crater_Homer",
			"Crater_Hommel",
			"Crater_Hooke",
			"Crater_Hopmann",
			"Crater_Hornsby",
			"Crater_Horrebow",
			"Crater_Horrocks",
			"Crater_Hortensius",
			"Crater_Houssay",
			"Crater_Houtermans",
			"Crater_Houzeau",
			"Crater_Hubble",
			"Crater_Huggins",
			"Crater_Hugo",
			"Crater_Humason",
			"Crater_Humboldt",
			"Crater_Hume",
			"Crater_Husband",
			"Crater_Hussein",
			"Crater_Hutton",
			"Crater_Huxley",
			"Crater_Hyginus",
			"Crater_Hypatia",
			"Crater_Ian",
			"Crater_Ibn_Bajja",
			"Crater_Ibn_Battuta",
			"Crater_Ibn_Firnas",
			"Crater_Ibn-Rushd",
			"Crater_Ibn_Yunus",
			"Crater_Icarus",
			"Crater_Ideler",
			"Crater_Idelson",
			"Crater_Igor",
			"Crater_Ilin",
			"Crater_Ina",
			"Crater_Ingalls",
			"Crater_Inghirami",
			"Crater_Innes",
			"Crater_Ioffe",
			"Crater_Isabel",
			"Crater_Isaev",
			"Crater_Isidorus",
			"Crater_Isis",
			"Crater_Ivan",
			"Crater_Izsak",
			"Crater_Jaci",
			"Crater_Jackson",
			"Crater_Jacobi",
			"Crater_James",
			"Crater_Jansen",
			"Crater_Jansky",
			"Crater_Janssen",
			"Crater_Jarvis",
			"Crater_Jeans",
			"Crater_Jehan",
			"Crater_Jenkins",
			"Crater_Jenner",
			"Crater_Jerik",
			"Crater_J_Herschel",
			"Crater_Johnson",
			"Crater_Joliot",
			"Crater_Jomo",
			"Crater_José",
			"Crater_Joule",
			"Crater_Joy",
			"Crater_Jules_Verne",
			"Crater_Julienne",
			"Crater_Julius_Caesar",
			"Crater_Kaiser",
			"Crater_Kamerlingh_Onnes",
			"Crater_Kane",
			"Crater_Kant",
			"Crater_Kao",
			"Crater_Kapteyn",
			"Crater_Karima",
			"Crater_Karpinskiy",
			"Crater_Karrer",
			"Crater_Kasper",
			"Crater_Kästner",
			"Crater_Katchalsky",
			"Crater_Kathleen",
			"Crater_Kearons",
			"Crater_Keeler",
			"Crater_Kekulé",
			"Crater_Keldysh",
			"Crater_Kepínski",
			"Crater_Kepler",
			"Crater_Khvolson",
			"Crater_Kibalchich",
			"Crater_Kidinnu",
			"Crater_Kies",
			"Crater_Kiess",
			"Crater_Kimura",
			"Crater_Kinau",
			"Crater_King",
			"Crater_Kira",
			"Crater_Kirch",
			"Crater_Kircher",
			"Crater_Kirchhoff",
			"Crater_Kirkwood",
			"Crater_Klaproth",
			"Crater_Klein",
			"Crater_Kleymenov",
			"Crater_Klute",
			"Crater_Knox-Shaw",
			"Crater_Koch",
			"Crater_Kocher",
			"Crater_Kohlschütter",
			"Crater_Kolhörster",
			"Crater_Kolya",
			"Crater_Komarov",
			"Crater_Kondratyuk",
			"Crater_König",
			"Crater_Konoplev",
			"Crater_Konstantinov",
			"Crater_Kopff",
			"Crater_Korolev",
			"Crater_Kosberg",
			"Crater_Kostinskiy",
			"Crater_Kostya",
			"Crater_Kovalevskaya",
			"Crater_Kovalskiy",
			"Crater_Kozyrev",
			"Crater_Krafft",
			"Crater_Kramarov",
			"Crater_Kramers",
			"Crater_Krasnov",
			"Crater_Krasovskiy",
			"Crater_Kreiken",
			"Crater_Krieger",
			"Crater_Krogh",
			"Crater_Krusenstern",
			"Crater_Krylov",
			"Crater_Kugler",
			"Crater_Kuhn",
			"Crater_Kuiper",
			"Crater_Kulik",
			"Crater_Kundt",
			"Crater_Kunowsky",
			"Crater_Kuo_Shou_Ching",
			"Crater_Kurchatov",
			"Crater_La_Caille",
			"Crater_Lacchini",
			"Crater_La_Condamine",
			"Crater_Lacroix",
			"Crater_Lade",
			"Crater_Lagalla",
			"Crater_Lagrange",
			"Crater_Lalande",
			"Crater_Lallemand",
			"Crater_Lamarck",
			"Crater_Lamb",
			"Crater_Lambert",
			"Crater_Lamé",
			"Crater_Lamèch",
			"Crater_Lamont",
			"Crater_Lampland",
			"Crater_Landau",
			"Crater_Lander",
			"Crater_Landsteiner",
			"Crater_Lane",
			"Crater_Langemak",
			"Crater_Langevin",
			"Crater_Langley",
			"Crater_Langmuir",
			"Crater_Langrenus",
			"Crater_Lansberg",
			"Crater_La_Pérouse",
			"Crater_Larmor",
			"Crater_Lassell",
			"Crater_Laue",
			"Crater_Lauritsen",
			"Crater_Laveran",
			"Crater_Lavoisier",
			"Crater_Lawrence",
			"Crater_L_Clark",
			"Crater_Leakey",
			"Crater_Leavitt",
			"Crater_Lebedev",
			"Crater_Lebedinskiy",
			"Crater_Lebesgue",
			"Crater_Lee",
			"Crater_Leeuwenhoek",
			"Crater_Legendre",
			"Crater_Le_Gentil",
			"Crater_Lehmann",
			"Crater_Leibnitz",
			"Crater_Lemaître",
			"Crater_Le_Monnier",
			"Crater_Lenard",
			"Crater_Lents",
			"Crater_Leonid",
			"Crater_Leonov",
			"Crater_Lepaute",
			"Crater_Letronne",
			"Crater_Leucippus",
			"Crater_Leuschner",
			"Crater_Lev",
			"Crater_Le_Verrier",
			"Crater_Levi-Civita",
			"Crater_Levi-Montalcini",
			"Crater_Lewis",
			"Crater_Lexell",
			"Crater_Ley",
			"Crater_Lhamu",
			"Crater_Li_Bing",
			"Crater_Licetus",
			"Crater_Lichtenberg",
			"Crater_Lick",
			"Crater_Liebig",
			"Crater_Lilius",
			"Crater_Linda",
			"Crater_Lindbergh",
			"Crater_Lindblad",
			"Crater_Lindenau",
			"Crater_Lindsay",
			"Crater_Linné",
			"Crater_Liouville",
			"Crater_Li_Po",
			"Crater_Lippershey",
			"Crater_Lippmann",
			"Crater_Lipskiy",
			"Crater_Li_Shizhen",
			"Crater_Litke",
			"Crater_Littrow",
			"Crater_Liu_Hui",
			"Crater_Lobachevskiy",
			"Crater_Lockyer",
			"Crater_Lodygin",
			"Crater_Loewy",
			"Crater_Lohrmann",
			"Crater_Lohse",
			"Crater_Lomonosov",
			"Crater_Longfellow",
			"Crater_Longomontanus",
			"Crater_Lorca",
			"Crater_Lorentz",
			"Crater_Louise",
			"Crater_Louville",
			"Crater_Love",
			"Crater_Lovelace",
			"Crater_Lovell",
			"Crater_Lowell",
			"Crater_Lubbock",
			"Crater_Lubiniezky",
			"Crater_Lucian",
			"Crater_Lucretius",
			"Crater_Ludwig",
			"Crater_Lundmark",
			"Crater_Luther",
			"Crater_Lyapunov",
			"Crater_Lyell",
			"Crater_Lyman",
			"Crater_Lyot",
			"Crater_Mach",
			"Crater_Maclaurin",
			"Crater_Maclear",
			"Crater_MacMillan",
			"Crater_Macrobius",
			"Crater_Mädler",
			"Crater_Maestlin",
			"Crater_Magelhaens",
			"Crater_Maginus",
			"Crater_Main",
			"Crater_Mairan",
			"Crater_Maksutov",
			"Crater_Malapert",
			"Crater_Malinkin",
			"Crater_Mallet",
			"Crater_Malyy",
			"Crater_Mandelshtam",
			"Crater_M_Anderson",
			"Crater_Manilius",
			"Crater_Mann",
			"Crater_Manners",
			"Crater_Manuel",
			"Crater_Manzinus",
			"Crater_Maraldi",
			"Crater_Marci",
			"Crater_Marconi",
			"Crater_Marco_Polo",
			"Crater_Marinus",
			"Crater_Mariotte",
			"Crater_Marius",
			"Crater_Markov",
			"Crater_Marth",
			"Crater_Marvin",
			"Crater_Mary",
			"Crater_Masina",
			"Crater_Maskelyne",
			"Crater_Mason",
			"Crater_Maunder",
			"Crater_Maupertuis",
			"Crater_Maurolycus",
			"Crater_Maury",
			"Crater_Mavis",
			"Crater_Maxwell",
			"Crater_McAdie",
			"Crater_McAuliffe",
			"Crater_McClure",
			"Crater_McCool",
			"Crater_McDonald",
			"Crater_McKellar",
			"Crater_McLaughlin",
			"Crater_McMath",
			"Crater_McNair",
			"Crater_McNally",
			"Crater_Mechnikov",
			"Crater_Mee",
			"Crater_Mees",
			"Crater_Meggers",
			"Crater_Meitner",
			"Crater_Melissa",
			"Crater_Mendel",
			"Crater_Mendeleev",
			"Crater_Menelaus",
			"Crater_Menzel",
			"Crater_Mercator",
			"Crater_Mercurius",
			"Crater_Merrill",
			"Crater_Mersenius",
			"Crater_Meshcherskiy",
			"Crater_Messala",
			"Crater_Messier",
			"Crater_Metius",
			"Crater_Meton",
			"Crater_Mezentsev",
			"Crater_Michael",
			"Crater_Michelson",
			"Crater_Milanković",
			"Crater_Milichius",
			"Crater_Miller",
			"Crater_Millikan",
			"Crater_Mills",
			"Crater_Milne",
			"Crater_Milton",
			"Crater_Mineur",
			"Crater_Minkowski",
			"Crater_Minnaert",
			"Crater_Mirzakhani",
			"Crater_Mitchell",
			"Crater_Mitra",
			"Crater_Möbius",
			"Crater_Mohorovičić",
			"Crater_Moigno",
			"Crater_Moiseev",
			"Crater_Moissan",
			"Crater_Moltke",
			"Crater_Monge",
			"Crater_Monira",
			"Crater_Montaigne",
			"Crater_Montanari",
			"Crater_Montesquieu",
			"Crater_Montgolfier",
			"Crater_Moore",
			"Crater_Moretus",
			"Crater_Morley",
			"Crater_Morozov",
			"Crater_Morse",
			"Crater_Moseley",
			"Crater_Mösting",
			"Crater_Mouchez",
			"Crater_Moulton",
			"Crater_Müller",
			"Crater_Murakami",
			"Crater_Murchison",
			"Crater_Mutus",
			"Crater_Nagaoka",
			"Crater_Nam_Byeong-Cheol",
			"Crater_Nansen",
			"Crater_Naonobu",
			"Crater_Nasireddin",
			"Crater_Nasmyth",
			"Crater_Nassau",
			"Crater_Natasha",
			"Crater_Naumann",
			"Crater_Neander",
			"Crater_Nearch",
			"Crater_Necho",
			"Crater_Nefedev",
			"Crater_Neison",
			"Crater_Neper",
			"Crater_Nernst",
			"Crater_Neujmin",
			"Crater_Neumayer",
			"Crater_Newcomb",
			"Crater_Newton",
			"Crater_Nicholson",
			"Crater_Nicolai",
			"Crater_Nicollet",
			"Crater_Nielsen",
			"Crater_Niepce",
			"Crater_Nijland",
			"Crater_Nikolaev",
			"Crater_Nikolya",
			"Crater_Nishina",
			"Crater_Nobel",
			"Crater_Nobile",
			"Crater_Nobili",
			"Crater_Nöggerath",
			"Crater_Nonius",
			"Crater_Norman",
			"Crater_Nöther",
			"Crater_Novalis",
			"Crater_Numerov",
			"Crater_Nunn",
			"Crater_Nušl",
			"Crater_Oberth",
			"Crater_Obruchev",
			"Crater_ODay",
			"Crater_Oenopides",
			"Crater_Oersted",
			"Crater_Ohm",
			"Crater_Oken",
			"Crater_Olbers",
			"Crater_Olcott",
			"Crater_Olivier",
			"Crater_Omar_Khayyam",
			"Crater_Onizuka",
			"Crater_Opelt",
			"Crater_Oppenheimer",
			"Crater_Oppolzer",
			"Crater_Oresme",
			"Crater_Orlov",
			"Crater_Orontius",
			"Crater_Osama",
			"Crater_Osiris",
			"Crater_Osman",
			"Crater_Ostwald",
			"Crater_Palisa",
			"Crater_Palitzsch",
			"Crater_Pallas",
			"Crater_Palmieri",
			"Crater_Paneth",
			"Crater_Pannekoek",
			"Crater_Papaleksi",
			"Crater_Paracelsus",
			"Crater_Paraskevopoulos",
			"Crater_Parenago",
			"Crater_Parkhurst",
			"Crater_Parrot",
			"Crater_Parry",
			"Crater_Parsons",
			"Crater_Pascal",
			"Crater_Paschen",
			"Crater_Pasteur",
			"Crater_Patricia",
			"Crater_Patsaev",
			"Crater_Pauli",
			"Crater_Pavlov",
			"Crater_Pawsey",
			"Crater_Peary",
			"Crater_Pease",
			"Crater_Peek",
			"Crater_Peirce",
			"Crater_Peirescius",
			"Crater_Pei_Xiu",
			"Crater_Pentland",
			"Crater_Perelman",
			"Crater_Perepelkin",
			"Crater_Perey",
			"Crater_Perkin",
			"Crater_Perrine",
			"Crater_Petavius",
			"Crater_Petermann",
			"Crater_Peters",
			"Crater_Petit",
			"Crater_Petrie",
			"Crater_Petropavlovskiy",
			"Crater_Petrov",
			"Crater_Pettit",
			"Crater_Petzval",
			"Crater_Phillips",
			"Crater_Philolaus",
			"Crater_Phocylides",
			"Crater_Piazzi",
			"Crater_Piazzi_Smyth",
			"Crater_Picard",
			"Crater_Piccolomini",
			"Crater_Pickering",
			"Crater_Pictet",
			"Crater_Pierazzo",
			"Crater_Pikelner",
			"Crater_Pilâtre",
			"Crater_Pingré",
			"Crater_Pirandello",
			"Crater_Pirquet",
			"Crater_Pitatus",
			"Crater_Pitiscus",
			"Crater_Pizzetti",
			"Crater_Plana",
			"Crater_Planck",
			"Crater_Planté",
			"Crater_Plaskett",
			"Crater_Plato",
			"Crater_Playfair",
			"Crater_Plinius",
			"Crater_Plummer",
			"Crater_Plutarch",
			"Crater_Poczobutt",
			"Crater_Pogson",
			"Crater_Poincaré",
			"Crater_Poinsot",
			"Crater_Poisson",
			"Crater_Polybius",
			"Crater_Polzunov",
			"Crater_Pomortsev",
			"Crater_Poncelet",
			"Crater_Pons",
			"Crater_Pontanus",
			"Crater_Pontécoulant",
			"Crater_Pope",
			"Crater_Popov",
			"Crater_Porter",
			"Crater_Posidonius",
			"Crater_Poynting",
			"Crater_Prager",
			"Crater_Prandtl",
			"Crater_Priestley",
			"Crater_Prinz",
			"Crater_Priscilla",
			"Crater_Proclus",
			"Crater_Proctor",
			"Crater_Protagoras",
			"Crater_Ptolemaeus",
			"Crater_Puiseux",
			"Crater_Pupin",
			"Crater_Purbach",
			"Crater_Purkyně",
			"Crater_Pythagoras",
			"Crater_Pytheas",
			"Crater_Quetelet",
			"Crater_Rabbi_Levi",
			"Crater_Racah",
			"Crater_Racine",
			"Crater_Raimond",
			"Crater_Raman",
			"Crater_Ramon",
			"Crater_Ramsay",
			"Crater_Ramsden",
			"Crater_Rankine",
			"Crater_Raspletin",
			"Crater_Ravi",
			"Crater_Rayet",
			"Crater_Rayleigh",
			"Crater_Razumov",
			"Crater_Réaumur",
			"Crater_Recht",
			"Crater_Regiomontanus",
			"Crater_Regnault",
			"Crater_Reichenbach",
			"Crater_Reimarus",
			"Crater_Reiner",
			"Crater_Reinhold",
			"Crater_Repsold",
			"Crater_Resnik",
			"Crater_Respighi",
			"Crater_Rhaeticus",
			"Crater_Rheita",
			"Crater_Riccioli",
			"Crater_Riccius",
			"Crater_Ricco",
			"Crater_Richards",
			"Crater_Richardson",
			"Crater_Riedel",
			"Crater_Riemann",
			"Crater_Ritchey",
			"Crater_Rittenhouse",
			"Crater_Ritter",
			"Crater_Ritz",
			"Crater_Robert",
			"Crater_Roberts",
			"Crater_Robertson",
			"Crater_Robinson",
			"Crater_Rocca",
			"Crater_Rocco",
			"Crater_Roche",
			"Crater_Romeo",
			"Crater_Römer",
			"Crater_Röntgen",
			"Crater_Rosa",
			"Crater_Rosenberger",
			"Crater_Ross",
			"Crater_Rosse",
			"Crater_Rosseland",
			"Crater_Rost",
			"Crater_Rothmann",
			"Crater_Rowland",
			"Crater_Rozhdestvenskiy",
			"Crater_Rubin",
			"Crater_Rumford",
			"Crater_Runge",
			"Crater_Russell",
			"Crater_Ruth",
			"Crater_Rutherford",
			"Crater_Rutherfurd",
			"Crater_Rydberg",
			"Crater_Ryder",
			"Crater_Rynin",
			"Crater_Sabatier",
			"Crater_Sabine",
			"Crater_Sacrobosco",
			"Crater_Saenger",
			"Crater_Šafařík",
			"Crater_Saha",
			"Crater_Salam",
			"Crater_Samir",
			"Crater_Sampson",
			"Crater_Sanford",
			"Crater_Santbech",
			"Crater_Santos-Dumont",
			"Crater_Sappho",
			"Crater_Sarabhai",
			"Crater_Sarton",
			"Crater_Sasserides",
			"Crater_Saunder",
			"Crater_Saussure",
			"Crater_Scaliger",
			"Crater_Schaeberle",
			"Crater_Scheele",
			"Crater_Scheiner",
			"Crater_Schiaparelli",
			"Crater_Schickard",
			"Crater_Schiller",
			"Crater_Schjellerup",
			"Crater_Schlesinger",
			"Crater_Schliemann",
			"Crater_Schlüter",
			"Crater_Schmidt",
			"Crater_Schneller",
			"Crater_Schomberger",
			"Crater_Schönfeld",
			"Crater_Schorr",
			"Crater_Schrödinger",
			"Crater_Schröter",
			"Crater_Schubert",
			"Crater_Schumacher",
			"Crater_Schuster",
			"Crater_Schwabe",
			"Crater_Schwarzschild",
			"Crater_Scobee",
			"Crater_Scoresby",
			"Crater_Scott",
			"Crater_Seares",
			"Crater_Secchi",
			"Crater_Sechenov",
			"Crater_Seeliger",
			"Crater_Segers",
			"Crater_Segner",
			"Crater_Seidel",
			"Crater_Seleucus",
			"Crater_Seneca",
			"Crater_Seyfert",
			"Crater_Shackleton",
			"Crater_Shahinaz",
			"Crater_Shaler",
			"Crater_Shapley",
			"Crater_Sharonov",
			"Crater_Sharp",
			"Crater_Shatalov",
			"Crater_Shayn",
			"Crater_Sheepshanks",
			"Crater_Shekhov",
			"Crater_Shen_Kuo",
			"Crater_Sherrington",
			"Crater_Shioli",
			"Crater_Shirakatsi",
			"Crater_Shi_Shen",
			"Crater_Shoemaker",
			"Crater_Short",
			"Crater_Shternberg",
			"Crater_Shuckburgh",
			"Crater_Shuleykin",
			"Crater_Siedentopf",
			"Crater_Sierpinski",
			"Crater_Sikorsky",
			"Crater_Silberschlag",
			"Crater_Simpelius",
			"Crater_Sinas",
			"Crater_Sirsalis",
			"Crater_Sisakyan",
			"Crater_Sita",
			"Crater_Sklodowska",
			"Crater_Slater",
			"Crater_Slava",
			"Crater_Slipher",
			"Crater_Slocum",
			"Crater_Smith",
			"Crater_Smithson",
			"Crater_Smoluchowski",
			"Crater_Snellius",
			"Crater_Sniadecki",
			"Crater_Soddy",
			"Crater_Somerville",
			"Crater_Sommerfeld",
			"Crater_Sömmering",
			"Crater_Song_Yingxing",
			"Crater_Sophocles",
			"Crater_Soraya",
			"Crater_Sosigenes",
			"Crater_South",
			"Crater_Spallanzani",
			"Crater_Spencer_Jones",
			"Crater_Spörer",
			"Crater_Spudis",
			"Crater_Spurr",
			"Crater_Stadius",
			"Crater_Stark",
			"Crater_Stearns",
			"Crater_Stebbins",
			"Crater_Stefan",
			"Crater_Stein",
			"Crater_Steinheil",
			"Crater_Steklov",
			"Crater_Stella",
			"Crater_Steno",
			"Crater_Sternfeld",
			"Crater_Stetson",
			"Crater_Stevinus",
			"Crater_Stewart",
			"Crater_Stiborius",
			"Crater_St_John",
			"Crater_Stöfler",
			"Crater_Stokes",
			"Crater_Stoletov",
			"Crater_Stoney",
			"Crater_Störmer",
			"Crater_Stose",
			"Crater_Strabo",
			"Crater_Stratton",
			"Crater_Street",
			"Crater_Strömgren",
			"Crater_Struve",
			"Crater_Subbotin",
			"Crater_Suess",
			"Crater_Sulpicius_Gallus",
			"Crater_Sumner",
			"Crater_Sundman",
			"Crater_Sung-Mei",
			"Crater_Susan",
			"Crater_Su_Song",
			"Crater_Svedberg",
			"Crater_Sverdrup",
			"Crater_Swann",
			"Crater_Swasey",
			"Crater_Swift",
			"Crater_Sylvester",
			"Crater_Szilard",
			"Crater_Tacchini",
			"Crater_Tacitus",
			"Crater_Tacquet",
			"Crater_Tai_Wei",
			"Crater_Taizo",
			"Crater_Talbot",
			"Crater_Tamm",
			"Crater_Tannerus",
			"Crater_Taruntius",
			"Crater_Tasso",
			"Crater_Taylor",
			"Crater_Tebbutt",
			"Crater_Teisserenc",
			"Crater_Tempel",
			"Crater_Ten_Bruggencate",
			"Crater_Tereshkova",
			"Crater_Tesla",
			"Crater_Thales",
			"Crater_Tharp",
			"Crater_Theaetetus",
			"Crater_Thebit",
			"Crater_Theiler",
			"Crater_Theon_Junior",
			"Crater_Theon_Senior",
			"Crater_Theophilus",
			"Crater_Theophrastus",
			"Crater_Thiel",
			"Crater_Thiessen",
			"Crater_Thomson",
			"Crater_Tianjin",
			"Crater_Tian_Shi",
			"Crater_Tikhomirov",
			"Crater_Tikhov",
			"Crater_Tiling",
			"Crater_Timaeus",
			"Crater_Timiryazev",
			"Crater_Timocharis",
			"Crater_Tiselius",
			"Crater_Tisserand",
			"Crater_Titius",
			"Crater_Titov",
			"Crater_T_Mayer",
			"Crater_Tolansky",
			"Crater_Tolstoy",
			"Crater_Tooley",
			"Crater_Torricelli",
			"Crater_Toscanelli",
			"Crater_Townley",
			"Crater_Tralles",
			"Crater_Triesnecker",
			"Crater_Trouvelot",
			"Crater_Trumpler",
			"Crater_Tsander",
			"Crater_Tseraskiy",
			"Crater_Tsinger",
			"Crater_Tsiolkovskiy",
			"Crater_Tsu_Chung-Chi",
			"Crater_Tucker",
			"Crater_Turner",
			"Crater_Tycho",
			"Crater_Tyndall",
			"Crater_Ukert",
			"Crater_Ulugh_Beigh",
			"Crater_Undest",
			"Crater_Urey",
			"Crater_Väisälä",
			"Crater_Valera",
			"Crater_Valier",
			"Crater_van_Albada",
			"Crater_Van_Biesbroeck",
			"Crater_Van_de_Graaff",
			"Crater_Van_den_Bergh",
			"Crater_van_den_Bos",
			"Crater_Van_der_Waals",
			"Crater_Van_Gent",
			"Crater_Van_Maanen",
			"Crater_van_Rhijn",
			"Crater_vant_Hoff",
			"Crater_Van_Vleck",
			"Crater_Van_Wijk",
			"Crater_Vasco_da_Gama",
			"Crater_Vashakidze",
			"Crater_Vasya",
			"Crater_Vaughan",
			"Crater_Vavilov",
			"Crater_Vega",
			"Crater_Vendelinus",
			"Crater_Vening_Meinesz",
			"Crater_Ventris",
			"Crater_Vera",
			"Crater_Vergil",
			"Crater_Vernadskiy",
			"Crater_Verne",
			"Crater_Vertregt",
			"Crater_Very",
			"Crater_Vesalius",
			"Crater_Vestine",
			"Crater_Vetchinkin",
			"Crater_Vieta",
			"Crater_Vilev",
			"Crater_Vinogradov",
			"Crater_Virchow",
			"Crater_Virtanen",
			"Crater_Vitello",
			"Crater_Vitruvius",
			"Crater_Vitya",
			"Crater_Viviani",
			"Crater_Vlacq",
			"Crater_Vogel",
			"Crater_Volkov",
			"Crater_Volta",
			"Crater_Voltaire",
			"Crater_Volterra",
			"Crater_von_Baeyer",
			"Crater_von_Behring",
			"Crater_von_Békésy",
			"Crater_von_Braun",
			"Crater_Von_der_Pahlen",
			"Crater_Von_Kármán",
			"Crater_Von_Neumann",
			"Crater_Von_Zeipel",
			"Crater_Voskresenskiy",
			"Crater_Walker",
			"Crater_Wallace",
			"Crater_Wallach",
			"Crater_Walter",
			"Crater_Walther",
			"Crater_Wan-Hoo",
			"Crater_Wapowski",
			"Crater_Wargentin",
			"Crater_Wargo",
			"Crater_Warner",
			"Crater_Waterman",
			"Crater_Watson",
			"Crater_Watt",
			"Crater_Watts",
			"Crater_W_Bond",
			"Crater_Webb",
			"Crater_Weber",
			"Crater_Wegener",
			"Crater_Weierstrass",
			"Crater_Weigel",
			"Crater_Weinek",
			"Crater_Weiss",
			"Crater_Werner",
			"Crater_Wexler",
			"Crater_Weyl",
			"Crater_Whewell",
			"Crater_Whipple",
			"Crater_White",
			"Crater_Wichmann",
			"Crater_Widmannstätten",
			"Crater_Wiechert",
			"Crater_Wiener",
			"Crater_Wildt",
			"Crater_Wilhelm",
			"Crater_Wilkins",
			"Crater_Williams",
			"Crater_Wilsing",
			"Crater_Wilson",
			"Crater_Winkler",
			"Crater_Winlock",
			"Crater_Winthrop",
			"Crater_Wöhler",
			"Crater_Wolf",
			"Crater_Wollaston",
			"Crater_Woltjer",
			"Crater_Wood",
			"Crater_Wright",
			"Crater_Wróblewski",
			"Crater_Wrottesley",
			"Crater_Wurzelbauer",
			"Crater_Wyld",
			"Crater_Xenophanes",
			"Crater_Xenophon",
			"Crater_Xu_Guangqi",
			"Crater_Xu_Xiake",
			"Crater_Yablochkov",
			"Crater_Yakovkin",
			"Crater_Yamamoto",
			"Crater_Yangel",
			"Crater_Yerkes",
			"Crater_Yoshi",
			"Crater_Young",
			"Crater_Zach",
			"Crater_Zagut",
			"Crater_Zähringer",
			"Crater_Zanstra",
			"Crater_Zasyadko",
			"Crater_Zeeman",
			"Crater_Zelinskiy",
			"Crater_Zeno",
			"Crater_Zernike",
			"Crater_Zhang_Yuzhe",
			"Crater_Zhang_Zhongjing",
			"Crater_Zhinyu",
			"Crater_Zhiritskiy",
			"Crater_Zhukovskiy",
			"Crater_Zinner",
			"Crater_Zi_Wei",
			"Crater_Zola",
			"Crater_Zöllner",
			"Crater_Zsigmondy",
			"Crater_Zucchius",
			"Crater_Zupus",
			"Crater_Zwicky"
			};
	
	public static final String[] STARS = {
			"Absolutno",
			"Acamar",
			"Achernar",
			"Achird",
			"Acrab",
			"Acrux",
			"Acubens",
			"Adhafera",
			"Adhara",
			"Adhil",
			"Ain",
			"Ainalrami",
			"Aiolos",
			"Al_Minliar_al_Asad",
			"Aladfar",
			"Alasia",
			"Alathfar",
			"Albaldah",
			"Albali",
			"Albireo",
			"Alchiba",
			"Alcor",
			"Alcyone",
			"Aldebaran",
			"Alderamin",
			"Aldhanab",
			"Aldhibah",
			"Aldulfin",
			"Alfirk",
			"Algedi",
			"Algenib",
			"Algieba",
			"Algol",
			"Algorab",
			"Alhena",
			"Alioth",
			"Aljanah",
			"Alkaid",
			"Alkalurops",
			"Alkaphrah",
			"Alkarab",
			"Alkes",
			"Almaaz",
			"Almach",
			"Alnair",
			"Alnasl",
			"Alnilam",
			"Alnitak",
			"Alniyat",
			"Alphard",
			"Alphecca",
			"Alpheratz",
			"Alpherg",
			"Alrakis",
			"Alrescha",
			"Alruba",
			"Alsafi",
			"Alsciaukat",
			"Alsephina",
			"Alshain",
			"Alshat",
			"Altair",
			"Altais",
			"Alterf",
			"Aludra",
			"Alula_Australis",
			"Alula_Borealis",
			"Alya",
			"Alzirr",
			"Amadioha",
			"Amansinaya",
			"Anadolu",
			"Añañuca",
			"Ancha",
			"Angetenar",
			"Aniara",
			"Ankaa",
			"Anser",
			"Antares",
			"Arcalís",
			"Arcturus",
			"Arkab_Posterior",
			"Arkab_Prior",
			"Arneb",
			"Ascella",
			"Asellus_Australis",
			"Asellus_Borealis",
			"Asellus_Primus",
			"Asellus_Secundus",
			"Asellus_Tertius",
			"Ashlesha",
			"Aspidiske",
			"Asterope",
			"Atakoraka",
			"Athebyne",
			"Atik",
			"Atlas",
			"Atria",
			"Avior",
			"Axólotl",
			"Ayeyarwady",
			"Azelfafage",
			"Azha",
			"Azmidi",
			"Baekdu",
			"Barnard's_Star",
			"Baten_Kaitos",
			"Batsũ̀",
			"Beemim",
			"Beid",
			"Belel",
			"Bélénos",
			"Bellatrix",
			"Berehynia",
			"Betelgeuse",
			"Bharani",
			"Bibhā",
			"Biham",
			"Bosona",
			"Botein",
			"Brachium",
			"Bubup",
			"Buna",
			"Bunda",
			"Canopus",
			"Capella",
			"Caph",
			"Castor",
			"Castula",
			"Cebalrai",
			"Ceibo",
			"Celaeno",
			"Cervantes",
			"Chalawan",
			"Chamukuy",
			"Chaophraya",
			"Chara",
			"Chasoň",
			"Chechia",
			"Chertan",
			"Citadelle",
			"Citalá",
			"Cocibolca",
			"Copernicus",
			"Cor_Caroli",
			"Cujam",
			"Cursa",
			"Dabih",
			"Dalim",
			"Danfeng",
			"Deneb",
			"Deneb_Algedi",
			"Denebola",
			"Diadem",
			"Dilmun",
			"Dingolay",
			"Diphda",
			"Dìwö",
			"Diya",
			"Dofida",
			"Dombay",
			"Dschubba",
			"Dubhe",
			"Dziban",
			"Ebla",
			"Edasich",
			"Electra",
			"Elgafar",
			"Elkurud",
			"Elnath",
			"Eltanin",
			"Emiw",
			"Enif",
			"Errai",
			"Fafnir",
			"Fang",
			"Fawaris",
			"Felis",
			"Felixvarela",
			"Filetdor",
			"Flegetonte",
			"Fomalhaut",
			"Formosa",
			"Franz",
			"Fulu",
			"Fumalsamakah",
			"Funi",
			"Furud",
			"Fuyue",
			"Gacrux",
			"Gakyid",
			"Gar",
			"Garnet_Star",
			"Geminga",
			"Giausar",
			"Gienah",
			"Ginan",
			"Gloas",
			"Gnomon",
			"Gomeisa",
			"Graffias",
			"Grumium",
			"Guahayona",
			"Gudja",
			"Gumala",
			"Guniibuu",
			"Hadar",
			"Haedus",
			"Hamal",
			"Hassaleh",
			"Hatysa",
			"Helvetios",
			"Heze",
			"Hoggar",
			"Homam",
			"Horna",
			"Hunahpú",
			"Hunor",
			"Iklil",
			"Illyrian",
			"Imai",
			"Inquill",
			"Intan",
			"Intercrus",
			"Irena",
			"Itonda",
			"Izar",
			"Jabbah",
			"Jishui",
			"Kaewkosin",
			"Kaffaljidhma",
			"Kalausi",
			"Kamuy",
			"Kang",
			"Karaka",
			"Kaus_Australis",
			"Kaus_Borealis",
			"Kaus_Media",
			"Kaveh",
			"Keid",
			"Khambalia",
			"Kitalpha",
			"Kochab",
			"Koeia",
			"Koit",
			"Komondor",
			"Kornephoros",
			"Kosjenka",
			"Kraz",
			"Kuma",
			"Kurhah",
			"La_Superba",
			"Larawag",
			"Lerna",
			"Lesath",
			"Libertas",
			"Lich",
			"Liesma",
			"Lilii_Borea",
			"Lionrock",
			"Lucilinburhuc",
			"Lusitânia",
			"Maasym",
			"Macondo",
			"Mago",
			"Mahasim",
			"Mahsati",
			"Maia",
			"Malmok",
			"Marfik",
			"Markab",
			"Markeb",
			"Márohu",
			"Marsic",
			"Maru",
			"Matar",
			"Matza",
			"Mazaalai",
			"Mebsuta",
			"Megrez",
			"Meissa",
			"Mekbuda",
			"Meleph",
			"Menkalinan",
			"Menkar",
			"Menkent",
			"Menkib",
			"Merak",
			"Merga",
			"Meridiana",
			"Merope",
			"Mesarthim",
			"Miaplacidus",
			"Mimosa",
			"Minchir",
			"Minelauva",
			"Mintaka",
			"Mira",
			"Mirach",
			"Miram",
			"Mirfak",
			"Mirzam",
			"Misam",
			"Mizar",
			"Moldoveanu",
			"Mönch",
			"Montuno",
			"Morava",
			"Moriah",
			"Mothallah",
			"Mouhoun",
			"Mpingo",
			"Muliphein",
			"Muphrid",
			"Muscida",
			"Musica",
			"Muspelheim",
			"Nahn",
			"Naledi",
			"Naos",
			"Nashira",
			"Násti",
			"Natasha",
			"Navi",
			"Nekkar",
			"Nembus",
			"Nenque",
			"Nervia",
			"Nihal",
			"Nikawiy",
			"Noquisi",
			"Nosaxa",
			"Nunki",
			"Nusakan",
			"Nushagak",
			"Nyamien",
			"Ogma",
			"Okab",
			"Orkaria",
			"Paikauhale",
			"Parumleo",
			"Peacock",
			"Petra",
			"Phact",
			"Phecda",
			"Pherkad",
			"Phoenicia",
			"Piautos",
			"Pincoya",
			"Pipirima",
			"Pipoltr",
			"Pleione",
			"Poerava",
			"Polaris",
			"Polaris_Australis",
			"Polis",
			"Pollux",
			"Porrima",
			"Praecipua",
			"Prima_Hyadum",
			"Procyon",
			"Propus",
			"Proxima_Centauri",
			"Ran",
			"Rana",
			"Rapeto",
			"Rasalas",
			"Rasalgethi",
			"Rasalhague",
			"Rastaban",
			"Regor",
			"Regulus",
			"Revati",
			"Rigel",
			"Rigil_Kentaurus",
			"Rosalíadecastro",
			"Rotanev",
			"Ruchbah",
			"Rukbat",
			"Sabik",
			"Saclateni",
			"Sadachbia",
			"Sadalbari",
			"Sadalmelik",
			"Sadalsuud",
			"Sadr",
			"Sagarmatha",
			"Saiph",
			"Salm",
			"Sāmaya",
			"Sansuna",
			"Sargas",
			"Sarin",
			"Sceptrum",
			"Scheat",
			"Schedar",
			"Secunda_Hyadum",
			"Segin",
			"Seginus",
			"Sham",
			"Shama",
			"Sharjah",
			"Shaula",
			"Sheliak",
			"Sheratan",
			"Sika",
			"Sirius",
			"Situla",
			"Skat",
			"Solaris",
			"Spica",
			"Sterrennacht",
			"Stribor",
			"Sualocin",
			"Subra",
			"Suhail",
			"Sulafat",
			"Syrma",
			"Tabit",
			"Taika",
			"Taiyangshou",
			"Taiyi",
			"Talitha",
			"Tangra",
			"Tania_Australis",
			"Tania_Borealis",
			"Tapecue",
			"Tarazed",
			"Tarf",
			"Taygeta",
			"Tegmine",
			"Tejat",
			"Terebellum",
			"Tevel",
			"Thabit",
			"Theemin",
			"Thuban",
			"Tiaki",
			"Tianguan",
			"Tianyi",
			"Timir",
			"Tislit",
			"Titawin",
			"Tojil",
			"Toliman",
			"Tonatiuh",
			"Torcular",
			"Tuiren",
			"Tupã",
			"Tupi",
			"Tureis",
			"Ukdah",
			"Uklun",
			"Unukalhai",
			"Unurgunite",
			"Uruk",
			"Uúba",
			"Vega",
			"Veritate",
			"Vindemiatrix",
			"Wasat",
			"Wattle",
			"Wazn",
			"Wezen",
			"Wouri",
			"Wurren",
			"Xamidimura",
			"Xihe",
			"Xuange",
			"Yed_Posterior",
			"Yed_Prior",
			"Yildun",
			"Zaniah",
			"Zaurak",
			"Zavijava",
			"Zembra",
			"Zhang",
			"Zibal",
			"Zosma",
			"Zubenelgenubi",
			"Zubenelhakrabi",
			"Zubeneschamali"
	};
	
	public static final String[] LAKES = {
			"Abashiri",
			"Abaya",
			"Aberdeen",
			"Abert",
			"Abhe",
			"Abijata",
			"Abitibi",
			"Aby_Lagoon",
			"Afambo",
			"Agua_Vermelha_Reservoir",
			"Aishihik",
			"Akan",
			"Alakol",
			"Albert",
			"Alexandrina",
			"Alicura_Reservoir",
			"Alumine",
			"Amadeus",
			"Amadjuak",
			"Amatitlan",
			"Amisk",
			"Amistad_Reservoir",
			"Ammersee",
			"Amutui_Quimey_Reservoir",
			"Annecy",
			"Apache",
			"Aral_Sea",
			"Arendsee",
			"Argentino",
			"Arroyito_Reservoir",
			"Asejire_Reservoir",
			"Athabasca",
			"Atlin",
			"Attersee",
			"Austin",
			"Awassa",
			"Aydarkul",
			"Ba_Be",
			"Baikal",
			"Baker",
			"Balaton",
			"Balbina_Reservoir",
			"Balkhash",
			"Balta_Alba",
			"Bangweulu",
			"Banyoles",
			"Baptiste",
			"Baringo",
			"Bariri_Reservoir",
			"Barra_Bonita_Reservoir",
			"Bartlett",
			"Bas_dOr",
			"Bassenthwaite",
			"Bato",
			"Batur",
			"Bayano",
			"Bear",
			"Becharof",
			"Bedok_Reservoir",
			"Beira",
			"Bergumermeer",
			"Beysehir",
			"Bhojtal",
			"Bienville",
			"Billings_Reservoir",
			"Billy_Chinook",
			"Biwa",
			"Bled",
			"Bogoria",
			"Boon_Tsagaan",
			"Boraped_Reservoir",
			"Bositeng",
			"Bratskoye_Reservoir",
			"Broa_Reservoir",
			"Buffalo_Pound",
			"Buhi",
			"Burera",
			"Burley_Griffin",
			"Buttle",
			"Buyr",
			"Cahora_Bassa",
			"Canandaigua",
			"Caniapiscau_Reservoir",
			"Canyon",
			"Capivara_Reservoir",
			"Caratasca_Lagoon",
			"Cardiel",
			"Casa_de_Piedra_Reservoir",
			"Caspian_Sea",
			"Cayuga",
			"Cedar",
			"Chad",
			"Chamo",
			"Champlain",
			"Changshou",
			"Chany",
			"Chao",
			"Chapala",
			"Charzykowskie",
			"Chascomus",
			"Chervonoje",
			"Chicot",
			"Chienghai",
			"Chilika",
			"Chilwa",
			"Chini",
			"Chiriqui_Lagoon",
			"Chivero",
			"Chuzenji",
			"Claire",
			"Clark",
			"Clearwaters",
			"Cochico",
			"Colhue_Huapi",
			"Colorado_Lagoon",
			"Como",
			"Conesus",
			"Coniston",
			"Constance",
			"Crane_Prairie_Reservoir",
			"Crater",
			"Cree",
			"Crescent",
			"Crummock",
			"Cyambwe",
			"Cyohoha_Sud",
			"Dal",
			"Darbandikhan",
			"Dargin",
			"Dauphin",
			"Dead_Sea",
			"Demirkopru_Dam_Reservoir",
			"Derg",
			"Derwentwater",
			"Detroit",
			"Diamante_Lagoon",
			"Diamond",
			"Diefenbaker",
			"Dillon",
			"Dneprodzerzhinskoye_Reservoir",
			"Dobskie",
			"Dong",
			"Dongting",
			"Dore",
			"Driyviaty",
			"Druksiai",
			"Dubawnt",
			"Dusia",
			"Ebinur",
			"Edward",
			"Egridir",
			"Elmenteita",
			"Ennerdale",
			"Enriquillo",
			"Epecuen",
			"Erie",
			"Eskimos",
			"Evoron",
			"Eyasi",
			"Eyre",
			"Ezequiel_Ramos_Mexia_Reservoir",
			"Fagnano};",
			"Faguibine",
			"Falcon_International_Reservoir",
			"Fateh_Sagar",
			"Fern_Ridge",
			"Fitri",
			"Flathead",
			"Fontana",
			"Frome",
			"Futalaufquen",
			"G._Dimitrov",
			"Gairdner",
			"Galstas",
			"Garda",
			"Garrow",
			"Gatun",
			"General_Carrera",
			"Geneva",
			"George",
			"Geranimovas-Ilzas",
			"Gitaru_Reservoir",
			"Gods",
			"Goose",
			"Gorkovskoye_Reservoir",
			"Gouin",
			"Granby",
			"Great_Bear",
			"Great_Central",
			"Great_Salt",
			"Great_Slave",
			"Green_Mountain_Reservoir",
			"Guiers",
			"Guri_Reservoir",
			"Hachiro",
			"Hago",
			"Hamana",
			"Hammer",
			"Hancza",
			"Har",
			"Har_Us",
			"Hartbeespoort_Dam_Reservoir",
			"Harveys",
			"Haweswater",
			"Hazen",
			"Helmand",
			"Hemlock",
			"Hirfanli_Dam_Reservoir",
			"Hjalmaren",
			"Honeoye",
			"Hovsgol",
			"Huechulaufquen",
			"Hulun",
			"Hungtze",
			"Huron",
			"Hyargas",
			"Ibera",
			"Ibitinga_Reservoir",
			"Ihema",
			"Ikeda",
			"Ilha_Solteira_Reservoir",
			"Iliamna",
			"Ilmen",
			"Imandrovskoye_Reservoir",
			"Inari",
			"Inawashiro",
			"Inba-numa",
			"Inle",
			"Island",
			"Issyk-Kool",
			"Istada",
			"Itaipu_Reservoir",
			"Izabal",
			"Jatiluhur",
			"Jerid",
			"Junin",
			"Jupia_Reservoir",
			"Jurumirin_Reservoir",
			"Kairakkumskoye_Reservoir",
			"Kakhovskoye_Reservoir",
			"Kamafusa_Reservoir",
			"Kamburu_Reservoir",
			"Kamloops",
			"Kamskoye_Reservoir",
			"Kanevskoye_Reservoir",
			"Kanhargaov_Reservoir",
			"Kaoyu",
			"Kaptchagayskoye_Reservoir",
			"Kariba",
			"Kasba",
			"Kasumigaura",
			"Kawaguchi",
			"Keban_Dam_Reservoir",
			"Kejimkujik",
			"Kenyir_Reservoir",
			"Keuka",
			"Kezar",
			"Khanka",
			"Khantayskoye",
			"Kievskoye_Reservoir",
			"Kimbere_Reservoir",
			"Kindaruma_Reservoir",
			"Kinneret",
			"Kisajno",
			"Kivu",
			"Kivumba",
			"Kizaki",
			"Knyazhegubskoye_Reservoir",
			"Kojima",
			"Koka",
			"Kolleru",
			"Kootenay",
			"Krasnoyarskoye_Reservoir",
			"Krementchugskoye_Reservoir",
			"Kujbyshevskoe_Reservoir",
			"Kulundinskoye",
			"Kurisches_Bay",
			"Kurunegala_Reservoir",
			"Kyaring",
			"Kyoga",
			"La_Grande_2_Reservoir",
			"La_Grande_3_Reservoir",
			"La_Grande_4_Reservoir",
			"La_Plata",
			"Lacar",
			"Ladoga",
			"Laguna_de_Bay",
			"Lam_Ta_Khong_Reservoir",
			"Lanao",
			"Lesser_Slave",
			"Llanquihue",
			"Loch_Awe",
			"Loch_Leven",
			"Loch_Lomond",
			"Loch_Morar",
			"Loch_Ness",
			"Loch_Shiel",
			"Loktak",
			"Lop_Nor",
			"Los_Barreales_Reservoir",
			"Lough_Beg",
			"Lough_Neagh",
			"Lower",
			"Lower_Lough_Erne",
			"Lower_Seletar_Reservoir",
			"Luang_Sea",
			"Lubans",
			"Lucerne",
			"Lugano",
			"Lugu",
			"Lukomskoje",
			"Lunzer_See",
			"Mafu",
			"Maggiore",
			"Mai-Ndombe",
			"Malaren",
			"Malawi",
			"Mallasvesi",
			"Managua",
			"Manasbal",
			"Manicouagan_Reservoir",
			"Manitoba",
			"Manzala",
			"Mar_Chiquita",
			"Maracaibo",
			"Mari_Menuco_Reservoir",
			"Martre",
			"Mashu",
			"Masinga_Reservoir",
			"Massawippi",
			"Matano",
			"Mead",
			"Melville",
			"Memphremagog",
			"Mendota",
			"Menendez",
			"Michigan",
			"Michikamau",
			"Mihindi",
			"Milford_Reservoir",
			"Mille_Lacs",
			"Mingetchaurskoye_Reservoir",
			"Miquelon",
			"Mirim_Lagoon",
			"Mistassini",
			"Miyun_Reservoir",
			"Mjosa",
			"Mondsee",
			"Mono",
			"Moose",
			"Motosu",
			"Mozhaysk_Reservoir",
			"Mpanga",
			"Mugesera",
			"Muhazi",
			"Murray",
			"Muskoka",
			"Musters_Lago",
			"Mweru",
			"Nagase_Reservoir",
			"Nahuel_Huapi",
			"Naivasha",
			"Nakuru",
			"Nam_Ngum_Reservoir",
			"Namak",
			"Namu",
			"Naroch",
			"Nasho",
			"Nasser",
			"Natron",
			"Netilling",
			"Neusiedler_See",
			"Ngoring",
			"Nicaragua",
			"Niegocin",
			"Nipigon",
			"Nipissing",
			"Nojiri",
			"Northern_Mamry",
			"Northwood",
			"Nova_Avanhandava_Reservoir",
			"Novosibirskoye_Reservoir",
			"Nubia",
			"Nueltin",
			"O_Higgins",
			"Obelija",
			"Ogawara",
			"Oguta",
			"Ohrid",
			"Okanagan",
			"Okeechobee",
			"Okutama_Reservoir",
			"Onega",
			"Ontario",
			"Opinaca_Reservoir",
			"Orta",
			"Oulu",
			"Owasco",
			"Oze",
			"Paajarvi",
			"Paanajarvi",
			"Paijanne",
			"Pangong",
			"Parakrama",
			"Patos_Lagoon",
			"Patzcuaro",
			"Peipus",
			"Pellegrini",
			"Phewa",
			"Piedra_Del_Aguila_Reservoir",
			"Pielinen",
			"Pipmuacan",
			"Plitvices",
			"Poechos_Reservoir",
			"Pomo",
			"Pontchartrain",
			"Poopo",
			"Porto_Primavera_Reservoir",
			"Powell",
			"Poyang",
			"Prespa",
			"Proletarskoye_Reservoir",
			"Promissao_Reservoir",
			"Puelo",
			"Pyasino",
			"Pyramid",
			"Qilin",
			"Qionghai",
			"Qullen",
			"Rainy",
			"Ranco",
			"Rara",
			"Red",
			"Ree",
			"Reindeer",
			"Rinihue",
			"Rio_Hondo_Reservoir",
			"Rio_Tercero_I_Reservoir",
			"Rivadavia",
			"Rocha",
			"Ronge",
			"Roosevelt",
			"Rosana_Reservoir",
			"Rotorua",
			"Ruhondo",
			"Rukwa",
			"Rwanyakizinga",
			"Rweru",
			"Rybinsk_Reservoir",
			"Sagami_Reservoir",
			"Sagar",
			"Saguaro",
			"Saguling",
			"Saimaa",
			"Saint-John",
			"Sake",
			"Salto_Grande",
			"Salto_Grande_Reservoir",
			"Salton_Sea",
			"Samuel_Reservoir",
			"San_Roque_Reservoir",
			"Sancha",
			"Sapanca",
			"Saratovskoye_Reservoir",
			"Saroma",
			"Sarygamysh",
			"Sasykkol",
			"Sayanskoye_Reservoir",
			"Segozero",
			"Selawik",
			"Seletyteniz",
			"Selingue",
			"Seneca",
			"Sentarum",
			"Seul",
			"Sevan",
			"Shala",
			"Shardara_Reservoir",
			"Sheksninskoye_Reservoir",
			"Shikotsu",
			"Shinji",
			"Shlavantas",
			"Shumarinai",
			"Shuswap",
			"Sibaya",
			"Simcoe",
			"Singkarak",
			"Skadar",
			"Skaha",
			"Slapy",
			"Smallwood_Reservoir",
			"Smith_Mountain",
			"Sniardwy",
			"Sobradinho_Reservoir",
			"Songkhla",
			"Southern_Indian",
			"St._Clair",
			"Starnberger_See",
			"Stechlin",
			"Superior",
			"Suwa",
			"Swamp_Tasek_Bera",
			"Szczecin_Lagoon",
			"Taal",
			"Tahoe",
			"Tai",
			"Taimyr",
			"Takiyuak",
			"Tana",
			"Tanganyika",
			"Tangra",
			"Taquarucu_Reservoir",
			"Tarbela_Reservoir",
			"Taupo",
			"Tazawa",
			"Tchardarinskoye_Reservoir",
			"Tega-numa",
			"Teli",
			"Tengiz",
			"Terhiyn_Tsagaan",
			"Terinam",
			"Terminos_Lagoon",
			"Teshekpuk",
			"Tharthar",
			"The_West",
			"Thingvalla",
			"Titicaca",
			"Tjeuke_meer",
			"Toba",
			"Todos_los_Santos",
			"Tonle_Sap",
			"Topopyozerskoye_Reservoir",
			"Torrens",
			"Towada",
			"Toya",
			"Trasimeno",
			"Traunsee",
			"Tres_Irmaos_Reservoir",
			"Trummen",
			"Tsimlyanskoye_Reservoir",
			"Tucurui_Reservoir",
			"Tumba",
			"Turkana",
			"Tuz",
			"Twins",
			"Ubinskoe",
			"Ubolratana_Reservoir",
			"Udawalawa_Reservoir",
			"Ullswater",
			"Ulu_Lepar_System",
			"Ulungur",
			"Upemba",
			"Upper_Klamath",
			"Upper_Lough_Erne",
			"Urmia",
			"Ust-Ilimskoye_Reservoir",
			"Uvildy",
			"Uvs",
			"Valencia",
			"Van",
			"Vanajavesi",
			"Vanern",
			"Varna",
			"Vattern",
			"Velencei-to",
			"Victoria",
			"Viedma",
			"Viluyskoe_Reservoir",
			"Volgogradskoye_Reservoir",
			"Volta",
			"Volta_Grande_Reservoir",
			"Volvi",
			"Voronegskoe_Reservoir",
			"Vortsjarv",
			"Votkinskoye_Reservoir",
			"Vygozersko-Ondskoye_Reservoir",
			"Wabamun",
			"Washington",
			"Wastwater",
			"Webster",
			"Weija",
			"Wei-shan",
			"West",
			"Western_Brook_Pond",
			"Wigry",
			"Williston",
			"Windermere",
			"Winnipeg",
			"Winnipegosis",
			"Wolfgangsee",
			"Wollaston",
			"Wood",
			"Woods",
			"Xavantes_Reservoir",
			"Xiaonanhai",
			"Yacyreta_P.P._Reservoir",
			"Yamdrok",
			"Yathkyed",
			"Yoan",
			"Ypacarai",
			"Yuqiao_Reservoir",
			"Zaysan",
			"Zeekoevlei",
			"Zeiskoye_Reservoir",
			"Ziway",
			"Zug",
			"Zurich_See"};
	
	
	public static final char MORSE_CHAR_DELIMITER = ' ';
	public static final char MORSE_WORD_DELIMITER = '/';
	
	public static final String[][] MORSE_2_STRING = {
			//LETTER
			{".-","A"},
			{"-...","B"},
			{"-.-.","C"},
			{"-..","D"},
			{".","E"},
			{"..-.","F"},
			{"--.","G"},
			{"....","H"},
			{"..","I"},
			{".---","J"},
			{"-.-","K"},
			{".-..","L"},
			{"--","M"},
			{"-.","N"},
			{"---","O"},
			{".--.","P"},
			{"--.-","Q"},
			{".-.","R"},
			{"...","S"},
			{"-","T"},
			{"..-","U"},
			{"...-","V"},
			{".--","W"},
			{"-..-","X"},
			{"-.--","Y"},
			{"--..","Z"},
			//DIGIT
			{"-----","0"},
			{".----","1"},
			{"..---","2"},
			{"...--","3"},
			{"....-","4"},
			{".....","5"},
			{"-....","6"},
			{"--...","7"},
			{"---..","8"},
			{"----.","9"},
			//PUNCTUATION MARK
			{".-...","&"},
			{".----.","'"},
			{".--.-.","@"},
			{"-.--.-",")"},
			{"-.--.","("},
			{"---...",":"},
			{"--..--",","},
			{"-...-","="},
			{"-.-.--","!"},
			{".-.-.-","."},
			{"-....-","-"},
			{"------..-.-----","%"},
			{".-.-.","+"},
			{".-..-.","\""},
			{"..--..","?"},
			{"-..-.","/"},
			//ACCENTED LETTER
			{".--.-","À"},
			//{".--.-","Å"},
			{".-.-","Ä"},
			//{".-.-","Ą"},
			//{".-.-","Æ"},
			{"-.-..","Ć"},
			//{"-.-..","Ĉ"},
			//{"-.-..","Ç"},
			{"----","CH"},
			//{"----","Ĥ"},
			//{"----","Š"},
			{"..-..","Đ"},
			//{"..-..","É"},
			//{"..-..","Ę"},
			{"..--.","Ð"},
			{".-..-","È"},
			//{".-..-","Ł"},
			{"--.-.","Ĝ"},
			{".---.","Ĵ"},
			{"--.--","Ń"},
			//{"--.--","Ñ"},
			{"---.","Ó"},
			//{"---.","Ö"},
			//{"---.","Ø"},
			{"...-...","Ś"},
			{"...-.","Ŝ"},
			{".--..","Þ"},
			{"..--","Ü"},
			//{"..--","Ŭ"},
			{"--..-.","Ź"},
			{"--..-","Ż"},
			//PROSIGN (some are in collision with accented letter, I prefer accented letter)
			{"........","<HH>"},
			{"...-.-","<VA>"},
			//{"...-.","<VE>"},
			//{".-.-","<AA>"},
			//{".-.-.","<AR>"},
			//{".-...","<AS>"},
			{"-...-.-","<BK>"},
			//{"-...-","<BT>"},
			{"-.-..-..","<CL>"},
			{"-.-.-","<CT>"},
			{"-..---","<DO>"},
			//{"-.-.-","<KA>"},
			//{"-.--.","<KN>"},
			//{"...-.-","<SK>"},
			//{"...-.","<SN>"},
			{"...---...","<SOS>"}

			
	};		
	
	public static final String[][] STRING_2_MORSE = {
			//LETTER
			{"A", ".-"},
			{"B", "-..."},
			{"C", "-.-."},
			{"D", "-.."},
			{"E", "."},
			{"F", "..-."},
			{"G", "--."},
			{"H", "...."},
			{"I", ".."},
			{"J", ".---"},
			{"K", "-.-"},
			{"L", ".-.."},
			{"M", "--"},
			{"N", "-."},
			{"O", "---"},
			{"P", ".--."},
			{"Q", "--.-"},
			{"R", ".-."},
			{"S", "..."},
			{"T", "-"},
			{"U", "..-"},
			{"V", "...-"},
			{"W", ".--"},
			{"X", "-..-"},
			{"Y", "-.--"},
			{"Z", "--.."},
			//DIGIT
			{"0", "-----"},
			{"1", ".----"},
			{"2", "..---"},
			{"3", "...--"},
			{"4", "....-"},
			{"5", "....."},
			{"6", "-...."},
			{"7", "--..."},
			{"8", "---.."},
			{"9", "----."},
			//PUNCTUATION MARK
			{"&", ".-..."},
			{"'", ".----."},
			{"@", ".--.-."},
			{")", "-.--.-"},
			{"(", "-.--."},
			{":", "---..."},
			{",", "--..--"},
			{"=", "-...-"},
			{"!", "-.-.--"},
			{".", ".-.-.-"},
			{"-", "-....-"},
			{"%", "------..-.-----"},
			{"+", ".-.-."},
			{"\"", ".-..-."},
			{"?", "..--.."},
			{"/", "-..-."},
			//ACCENTED LETTER
			{"À", ".--.-"},
			{"Å", ".--.-"},
			{"Ä", ".-.-"},
			{"Ą", ".-.-"},
			{"Æ", ".-.-"},
			{"Ć", "-.-.."},
			{"Ĉ", "-.-.."},
			{"Ç", "-.-.."},
			//{"CH", "----"},
			{"Ĥ", "----"},
			{"Š", "----"},
			{"Đ", "..-.."},
			{"É", "..-.."},
			{"Ę", "..-.."},
			{"Ð", "..--."},
			{"È", ".-..-"},
			{"Ł", ".-..-"},
			{"Ĝ", "--.-."},
			{"Ĵ", ".---."},
			{"Ń", "--.--"},
			{"Ñ", "--.--"},
			{"Ó", "---."},
			{"Ö", "---."},
			{"Ø", "---."},
			{"Ś", "...-..."},
			{"Ŝ", "...-."},
			{"Þ", ".--.."},
			{"Ü", "..--"},
			{"Ŭ", "..--"},
			{"Ź", "--..-."},
			{"Ż", "--..-"},			
			//PROSIGN
			{"<HH>", "........"},
			{"<VA>", "...-.-"},
			{"<VE>", "...-."},
			{"<AA>", ".-.-"},
			{"<AR>", ".-.-."},
			{"<AS>", ".-..."},
			{"<BK>", "-...-.-"},
			{"<BT>", "-...-"},
			{"<CL>", "-.-..-.."},
			{"<CT>", "-.-.-"},
			{"<DO>", "-..---"},
			{"<KA>", "-.-.-"},
			{"<KN>", "-.--."},
			{"<SK>", "...-.-"},
			{"<SN>", "...-."},
			{"<SOS>", "...---..."},
			//My added to be converted as non diacritics
			{"Á", ".-"},
			//{"Ä", ".-"},
			{"Č", "-.-."},
			{"Ď", "-.."},
			//{"É", "."},
			{"Ě", "."},
			{"Í", ".."},
			{"Ĺ", ".-.."},
			{"Ľ", ".-.."},
			{"Ň", "-."},
			//{"Ó", "---"},
			{"Ô", "---"},
			{"Ŕ", ".-."},
			{"Ř", ".-."},
			//{"Š", "..."},
			{"Ť", "-"},
			{"Ú", "..-"},
			{"Ů", "..-"},
			{"Ý", "-.--"},
			{"Ž", "--.."}
			//{"–", "-....-"},
			//{"„", ".-..-."},
			//{"“", ".-..-."}			
	};
	
	public static final Map<String, String> MORSE_2_STRING_MAP = new TreeMap<>(); 
	public static final Map<String, String> STRING_2_MORSE_MAP = new TreeMap<>();	
	static{
		String  key;
		for(String[] str : MORSE_2_STRING)
			if((key = MORSE_2_STRING_MAP.put(str[0], str[1]))!=null)
				System.out.println("Duplicate in morse code for: "+key+str[1]);
		
		for(String[] str : STRING_2_MORSE)
			if((key = STRING_2_MORSE_MAP.put(str[0], str[1]))!=null)
				System.out.println("Duplicate in morse string for: "+key);
	}
}

class RAFBuffer{
	static int BUFFER_SIZE = 65_536;
	RandomAccessFile raf;
	byte[] buffer = new byte[BUFFER_SIZE];	
	int position = 0;
	int max = 0;
	
	int read() throws IOException{
		if(position==max){
			max = raf.read(buffer);
			position = 0;
		}
		//System.out.println(position);
		return max>0?buffer[position++]&0xff:-1;
	}
	
	void write(int b) throws IOException{
		if(position==BUFFER_SIZE){
			flush();
		}
		buffer[position++] = (byte)b;		
	}
	
	void flush() throws IOException{
		raf.write(buffer, 0, position);
		position = 0;
	} 
	
	RAFBuffer(RandomAccessFile raf) {
		this.raf = raf;
	}
}