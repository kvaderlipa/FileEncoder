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
 * 		porozmyslat AES a prepis, co by bol unosware, ;-)) a tiez rekurzivne na adresare, nahodne heslo a cez public key zasifrovat a vytvorit subor so zasifrovanym heslom
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
	        		String lake = LAKES[RANDOM.nextInt(LAKES.length)];
	        		do{
	        			output = lake+(counter>0?("_"+counter):"")+".png";	        			
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
	        		img = CREATE_IMAGE(width, height, LAKE_WATER_RGB[0]+shade, LAKE_WATER_RGB[1]+shade, LAKE_WATER_RGB[2]+shade, null);
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
				i=bis.read();
			a = bis.read();
			if(a=='\n')
				a = bis.read();
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
	public static final int[] LAKE_WATER_RGB = {67, 111, 166};
	public static final int MAX_SHADE = 50;
	public static final double WIDTH_HEIGHT_RATIO = 16/9d;
	public static final double MAX_DATA_IN_IMAGE_RATIO = .9;//0.6;
	public static final double MIN_DATA_IN_IMAGE_RATIO = .6;//0.3;	
	public static final String[] LAKES = {
			"Chilwa",
			"Chad",
			"Sibaya",
			"Kariba",
			"Victoria",
			"Tanganyika",
			"Nakuru",
			"Chivero",
			"Guiers",
			"George",
			"Albert",
			"Edward",
			"Malawi",
			"Cahora_Bassa",
			"Kyoga",
			"Volta",
			"Zeekoevlei",
			"Oguta",
			"Nasser",
			"Turkana",
			"Abaya",
			"Abhe",
			"Abijata",
			"Asejire_Reservoir",
			"Awassa",
			"Bogoria",
			"Chamo",
			"Elmenteita",
			"Hartbeespoort_Dam_Reservoir",
			"Ihema",
			"Natron",
			"Koka",
			"Naivasha",
			"Rweru",
			"Nubia",
			"Shala",
			"Tana",
			"Ziway",
			"Afambo",
			"Aby_Lagoon",
			"Bangweulu",
			"Faguibine",
			"Kivu",
			"Selingue",
			"Manzala",
			"Mweru",
			"Rukwa",
			"Tumba",
			"Upemba",
			"Eyasi",
			"Weija",
			"Jerid",
			"Mai-Ndombe",
			"Baringo",
			"Yoan",
			"Teli",
			"Fitri",
			"Masinga_Reservoir",
			"Kamburu_Reservoir",
			"Gitaru_Reservoir",
			"Kindaruma_Reservoir",
			"Kimbere_Reservoir",
			"Burera",
			"Ruhondo",
			"Muhazi",
			"Mugesera",
			"Sake",
			"Rwanyakizinga",
			"Mihindi",
			"Hago",
			"Kivumba",
			"Nasho",
			"Cyambwe",
			"Mpanga",
			"Cyohoha_Sud",
			"Biwa",
			"Songkhla",
			"Rara",
			"Phewa",
			"Chuzenji",
			"Nagase_Reservoir",
			"Chao",
			"Miyun_Reservoir",
			"Kinneret",
			"Toba",
			"Dongting",
			"Dong",
			"Laguna_de_Bay",
			"Inawashiro",
			"Swamp_Tasek_Bera",
			"Shikotsu",
			"Toya",
			"Sagami_Reservoir",
			"Boraped_Reservoir",
			"Buhi",
			"Ebinur",
			"Alakol",
			"Boon_Tsagaan",
			"Bositeng",
			"Bhojtal",
			"Chany",
			"Chienghai",
			"Chilika",
			"Dead_Sea",
			"Akan",
			"Demirkopru_Dam_Reservoir",
			"Egridir",
			"Hirfanli_Dam_Reservoir",
			"Hulun",
			"Hungtze",
			"Hyargas",
			"Jatiluhur",
			"Kairakkumskoye_Reservoir",
			"Kaoyu",
			"Kaptchagayskoye_Reservoir",
			"Mashu",
			"Khanka",
			"Khantayskoye",
			"Caspian_Sea",
			"Kolleru",
			"Kulundinskoye",
			"Lanao",
			"Lugu",
			"Mafu",
			"Mingetchaurskoye_Reservoir",
			"Motosu",
			"Towada",
			"Namu",
			"Ngoring",
			"Novosibirskoye_Reservoir",
			"Poyang",
			"Pyasino",
			"Qilin",
			"Sasykkol",
			"Sayanskoye_Reservoir",
			"Seletyteniz",
			"Sevan",
			"Okutama_Reservoir",
			"Singkarak",
			"Aral_Sea",
			"Taimyr",
			"Chini",
			"Tazawa",
			"Tchardarinskoye_Reservoir",
			"Tengiz",
			"Terhiyn_Tsagaan",
			"Tuz",
			"Ulu_Lepar_System",
			"Kawaguchi",
			"Uvs",
			"Van",
			"Viluyskoe_Reservoir",
			"Wei-shan",
			"Xiaonanhai",
			"Yamdrok",
			"Zaysan",
			"Zeiskoye_Reservoir",
			"Beysehir",
			"Keban_Dam_Reservoir",
			"Tai",
			"Buyr",
			"Evoron",
			"Hammer",
			"Har",
			"Har_Us",
			"Helmand",
			"Istada",
			"Baikal",
			"Kyaring",
			"Luang_Sea",
			"Namak",
			"Pangong",
			"Tonle_Sap",
			"Tangra",
			"Tega-numa",
			"Terinam",
			"Batur",
			"Ubinskoe",
			"Ulungur",
			"Urmia",
			"Bato",
			"Ubolratana_Reservoir",
			"Sapanca",
			"Inba-numa",
			"Lower_Seletar_Reservoir",
			"Bedok_Reservoir",
			"Lam_Ta_Khong_Reservoir",
			"Loktak",
			"Tarbela_Reservoir",
			"Kurunegala_Reservoir",
			"Inle",
			"Ikeda",
			"Kenyir_Reservoir",
			"Matano",
			"Nam_Ngum_Reservoir",
			"Yuqiao_Reservoir",
			"Sagar",
			"Udawalawa_Reservoir",
			"Beira",
			"Lop_Nor",
			"Pomo",
			"Suwa",
			"Aydarkul",
			"Sentarum",
			"Sarygamysh",
			"Shardara_Reservoir",
			"Darbandikhan",
			"Tharthar",
			"Kizaki",
			"Shumarinai",
			"Hachiro",
			"Kasumigaura",
			"Saroma",
			"Kojima",
			"Dal",
			"Saguling",
			"Ogawara",
			"Oze",
			"Shinji",
			"Nojiri",
			"Hamana",
			"Parakrama",
			"Fateh_Sagar",
			"Lower",
			"Qionghai",
			"Sancha",
			"Changshou",
			"Hovsgol",
			"Abashiri",
			"The_West",
			"Balkhash",
			"Issyk-Kool",
			"Krasnoyarskoye_Reservoir",
			"Manasbal",
			"Taal",
			"Kamafusa_Reservoir",
			"Ust-Ilimskoye_Reservoir",
			"Bratskoye_Reservoir",
			"West",
			"Kanhargaov_Reservoir",
			"Ba_Be",
			"Tjeuke_meer",
			"Neusiedler_See",
			"Attersee",
			"Balaton",
			"Maggiore",
			"Zurich_See",
			"Geneva",
			"Loch_Ness",
			"Skadar",
			"Lunzer_See",
			"Windermere",
			"Trummen",
			"Malaren",
			"Hjalmaren",
			"Vattern",
			"Vanern",
			"Inari",
			"Pielinen",
			"Paijanne",
			"Paajarvi",
			"Arendsee",
			"Bassenthwaite",
			"Lough_Beg",
			"Sheksninskoye_Reservoir",
			"Bergumermeer",
			"Bled",
			"Peipus",
			"Como",
			"Coniston",
			"Ree",
			"Crummock",
			"Dargin",
			"Derwentwater",
			"Dneprodzerzhinskoye_Reservoir",
			"Dobskie",
			"Dusia",
			"Ennerdale",
			"Galstas",
			"Garda",
			"Gorkovskoye_Reservoir",
			"Derg",
			"Hancza",
			"Haweswater",
			"Prespa",
			"Ilmen",
			"Imandrovskoye_Reservoir",
			"Kakhovskoye_Reservoir",
			"Kamskoye_Reservoir",
			"Kanevskoye_Reservoir",
			"Kievskoye_Reservoir",
			"Kisajno",
			"Ammersee",
			"Knyazhegubskoye_Reservoir",
			"Krementchugskoye_Reservoir",
			"Loch_Leven",
			"Lower_Lough_Erne",
			"Lugano",
			"Mallasvesi",
			"Niegocin",
			"Northern_Mamry",
			"Starnberger_See",
			"Obelija",
			"Proletarskoye_Reservoir",
			"Lubans",
			"Lucerne",
			"Rybinsk_Reservoir",
			"Saratovskoye_Reservoir",
			"Segozero",
			"Shlavantas",
			"Loch_Morar",
			"Topopyozerskoye_Reservoir",
			"Traunsee",
			"Tsimlyanskoye_Reservoir",
			"Ullswater",
			"Upper_Lough_Erne",
			"Vanajavesi",
			"Mondsee",
			"Wolfgangsee",
			"Loch_Shiel",
			"Velencei-to",
			"Volgogradskoye_Reservoir",
			"Votkinskoye_Reservoir",
			"Vygozersko-Ondskoye_Reservoir",
			"Wastwater",
			"Wigry",
			"Zug",
			"Geranimovas-Ilzas",
			"Loch_Awe",
			"Kurisches_Bay",
			"Szczecin_Lagoon",
			"Oulu",
			"Saimaa",
			"Ohrid",
			"Charzykowskie",
			"Loch_Lomond",
			"Plitvices",
			"Trasimeno",
			"Sniardwy",
			"Stechlin",
			"Mjosa",
			"Constance",
			"Lough_Neagh",
			"Varna",
			"Onega",
			"Ladoga",
			"Mozhaysk_Reservoir",
			"Slapy",
			"Volvi",
			"Balta_Alba",
			"Thingvalla",
			"Paanajarvi",
			"G._Dimitrov",
			"Annecy",
			"Orta",
			"Vortsjarv",
			"Druksiai",
			"Naroch",
			"Chervonoje",
			"Lukomskoje",
			"Uvildy",
			"Voronegskoe_Reservoir",
			"Kujbyshevskoe_Reservoir",
			"Driyviaty",
			"Banyoles",
			"Mendota",
			"Tahoe",
			"Michigan",
			"Superior",
			"Huron",
			"Erie",
			"Ontario",
			"Winnipeg",
			"Washington",
			"Saint-John",
			"Conesus",
			"Hemlock",
			"Honeoye",
			"Canandaigua",
			"Keuka",
			"Seneca",
			"Cayuga",
			"Owasco",
			"Chicot",
			"Okeechobee",
			"Chiriqui_Lagoon",
			"Nicaragua",
			"Caratasca_Lagoon",
			"Izabal",
			"Enriquillo",
			"Terminos_Lagoon",
			"Amistad_Reservoir",
			"Pontchartrain",
			"Salton_Sea",
			"Twins",
			"Canyon",
			"Saguaro",
			"Apache",
			"Roosevelt",
			"Bartlett",
			"Mead",
			"Powell",
			"Mono",
			"Milford_Reservoir",
			"Green_Mountain_Reservoir",
			"Dillon",
			"Granby",
			"Great_Salt",
			"Goose",
			"Bear",
			"Upper_Klamath",
			"St._Clair",
			"Abert",
			"Crater",
			"Diamond",
			"Crescent",
			"Pyramid",
			"Crane_Prairie_Reservoir",
			"Fern_Ridge",
			"Billy_Chinook",
			"Detroit",
			"Bas_dOr",
			"Mille_Lacs",
			"Nipissing",
			"Flathead",
			"Red",
			"Gouin",
			"Muskoka",
			"Rainy",
			"Abitibi",
			"Woods",
			"Pipmuacan",
			"Nipigon",
			"Seul",
			"Manitoba",
			"Mistassini",
			"Dauphin",
			"Opinaca_Reservoir",
			"Kootenay",
			"Winnipegosis",
			"Cedar",
			"Melville",
			"Island",
			"Moose",
			"La_Grande_3_Reservoir",
			"La_Grande_4_Reservoir",
			"Smallwood_Reservoir",
			"Michikamau",
			"Gods",
			"Manicouagan_Reservoir",
			"Dore",
			"Ronge",
			"Bienville",
			"Lesser_Slave",
			"Clearwaters",
			"Cree",
			"Reindeer",
			"Becharof",
			"Wollaston",
			"Claire",
			"Aishihik",
			"Athabasca",
			"Atlin",
			"Iliamna",
			"Clark",
			"Kasba",
			"Nueltin",
			"Great_Slave",
			"Yathkyed",
			"Dubawnt",
			"Martre",
			"La_Grande_2_Reservoir",
			"Baker",
			"Aberdeen",
			"Amadjuak",
			"Takiyuak",
			"Netilling",
			"Selawik",
			"Eskimos",
			"Teshekpuk",
			"Falcon_International_Reservoir",
			"Williston",
			"Great_Bear",
			"Western_Brook_Pond",
			"Hazen",
			"Southern_Indian",
			"Great_Central",
			"Caniapiscau_Reservoir",
			"Northwood",
			"Smith_Mountain",
			"Champlain",
			"Webster",
			"Kezar",
			"Amatitlan",
			"Simcoe",
			"Baptiste",
			"Amisk",
			"Wabamun",
			"Miquelon",
			"Shuswap",
			"Memphremagog",
			"Massawippi",
			"Garrow",
			"Okanagan",
			"Wood",
			"Skaha",
			"Kamloops",
			"Buttle",
			"Kejimkujik",
			"Buffalo_Pound",
			"Diefenbaker",
			"Chapala",
			"Managua",
			"Harveys",
			"Taupo",
			"Burley_Griffin",
			"Rotorua",
			"Eyre",
			"Murray",
			"Frome",
			"Alexandrina",
			"Amadeus",
			"Austin",
			"Gairdner",
			"Torrens",
			"Broa_Reservoir",
			"Nahuel_Huapi",
			"Ezequiel_Ramos_Mexia_Reservoir",
			"Titicaca",
			"Valencia",
			"San_Roque_Reservoir",
			"Lacar",
			"Ypacarai",
			"Rocha",
			"Sobradinho_Reservoir",
			"Todos_los_Santos",
			"Salto_Grande",
			"Patzcuaro",
			"Maracaibo",
			"Gatun",
			"Bayano",
			"Guri_Reservoir",
			"Balbina_Reservoir",
			"Tucurui_Reservoir",
			"Poechos_Reservoir",
			"Samuel_Reservoir",
			"Junin",
			"Poopo",
			"Agua_Vermelha_Reservoir",
			"Volta_Grande_Reservoir",
			"Ilha_Solteira_Reservoir",
			"Tres_Irmaos_Reservoir",
			"Jupia_Reservoir",
			"Nova_Avanhandava_Reservoir",
			"Promissao_Reservoir",
			"Ibitinga_Reservoir",
			"Bariri_Reservoir",
			"Colorado_Lagoon",
			"Porto_Primavera_Reservoir",
			"Itaipu_Reservoir",
			"Barra_Bonita_Reservoir",
			"Taquarucu_Reservoir",
			"Rosana_Reservoir",
			"Capivara_Reservoir",
			"Salto_Grande_Reservoir",
			"Xavantes_Reservoir",
			"Jurumirin_Reservoir",
			"Billings_Reservoir",
			"Yacyreta_P.P._Reservoir",
			"Rio_Hondo_Reservoir",
			"Ibera",
			"Mar_Chiquita",
			"Patos_Lagoon",
			"Rio_Tercero_I_Reservoir",
			"Mirim_Lagoon",
			"Diamante_Lagoon",
			"Chascomus",
			"Cochico",
			"Epecuen",
			"Casa_de_Piedra_Reservoir",
			"Los_Barreales_Reservoir",
			"Mari_Menuco_Reservoir",
			"Pellegrini",
			"Alumine",
			"Arroyito_Reservoir",
			"Qullen",
			"Huechulaufquen",
			"Rinihue",
			"Piedra_Del_Aguila_Reservoir",
			"Alicura_Reservoir",
			"Ranco",
			"Llanquihue",
			"Puelo",
			"Rivadavia",
			"Menendez",
			"Futalaufquen",
			"Amutui_Quimey_Reservoir",
			"La_Plata",
			"Fontana",
			"Musters_Lago",
			"Colhue_Huapi",
			"General_Carrera",
			"O_Higgins",
			"Cardiel",
			"Viedma",
			"Argentino",
			"Fagnano"};
	
	
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