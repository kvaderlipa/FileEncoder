package fileEncoder;
import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

public class FileEncoder {
	// variables for access to see actual progress and for earlier terminating
	static boolean inProgress = false;	//status if working or not
	static boolean cancel = false;		//signal for canceling process
	static int progress = 0; 			// 0 to 100%	
	//
	
	
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
	
	public static void GEN_RANDOM_DATA_FILE(String output, long size, long seed) throws IOException {
		//
		inProgress = true;
		cancel = false;
		progress = 0;
		//
		Random r = new Random(seed);
		
		byte[] buffer = new byte[65536];
		
		// initialize
		for (int i = 0; i < 4963; i++)
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

	public static void SECURE_DELETE(String input, long seed) throws IOException {
		//
		inProgress = true;
		cancel = false;
		progress = 0;
		//
		Random r = new Random(seed);
		byte[] buffer = new byte[65536];
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
	
	public static void ONE_TIME_PAD(String input, String output, long seed) throws IOException {
		//
		inProgress = true;
		cancel = false;
		progress = 0;
		//
		Random r = new Random(seed);
		
		byte[] buffer = new byte[65536];
		byte[] xor = new byte[65536];
		
		// initialize
		for (int i = 0; i < 4963; i++)
			r.nextBytes(xor);
		
		int i;
                RandomAccessFile racr = new RandomAccessFile(input, "r");
                RandomAccessFile racw = new RandomAccessFile(output, "rw");
                
		while((i=racr.read(buffer))!=-1 && !cancel) {
			r.nextBytes(xor);
			for (int j = 0; j < i; j++)
				buffer[j] ^= xor[j]; 
			racw.write(buffer, 0, i);
			progress = (int)((double)racr.getFilePointer()/racr.length()*100);
		}
		racr.close();
		racw.close();	
		//		
		inProgress = false;
		//
	}
	
        public static int ENCODE_FILE_TO_IMAGE(String input, String imageFile, String output, long seed) throws Exception{            
    		//
    		inProgress = true;
    		cancel = false;
    		progress = 0;
    		cnt = 0;
    		//
        	BufferedImage img = null;
            try {
                img = ImageIO.read(new File(imageFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
            RandomAccessFile raf = new RandomAccessFile(input, "r");
            ImageByteStreamWriter ibsw = new ImageByteStreamWriter(img, seed);
            int b;
            int size = (int)raf.length();
            //write file length
            ibsw.write(size&255);
            ibsw.write(size>>8&255);
            ibsw.write(size>>16&255);
            ibsw.write(size>>24&255);
            input = new File(input).getName();
            //write name length
            size = input.length();
            ibsw.write(size&255);
            //write name of file
            for (int i = 0; i < input.length(); i++)
                ibsw.write((int)input.charAt(i));
            //write file data
            while((b=raf.read())!=-1 && !cancel){
                ibsw.write(b);
                progress = (int)((double)raf.getFilePointer()/raf.length()*100);
            }
            raf.close();
            //bit align, not needed if pixel mixing is used
            //ibsw.bitAlign();
            
            try {
                ImageIO.write(img, output.substring(output.lastIndexOf(".")+1), new File(output));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //    		
    		inProgress = false;
    		//
            return ibsw.getBitSize()+1;
        }
        
        public static void DECODE_FILE_FROM_IMAGE(String imageFile, String outputPath, long seed) throws Exception{
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
           
            ImageByteStreamReader ibsr = new ImageByteStreamReader(img, seed);            
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
            for (int i = 0; i < size && !cancel; i++){
            	raf.write(ibsr.read());   
            	progress = (int)((double)i/size*100);
            }
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
		byte[] buffer = new byte[65536];
		byte[] xor = new byte[65536];
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
	
	public static void main(String[] args) throws Exception {	
		int bitCount;
		long seed = 4963;
		
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
				if((args.length>=4)||(args.length>=3 && args[1].toLowerCase().equals("-d")) ) {					
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
						DECODE_FILE_FROM_IMAGE(args[2], path, seed);						
					}else{
						if(args.length>=5)
							seed = Long.parseLong(args[4]);
						bitCount = ENCODE_FILE_TO_IMAGE(args[1], args[2], args[3], seed);
						System.out.print("Used bits: "+bitCount);
					}															
				}else {					
					System.out.println("Program Arguments");
					System.out.println("1 - input data file - mandatory (file to be encoded)");
					System.out.println("2 - input image file - mandatory (medium use for encoding)");
					System.out.println("3 - output image file - mandatory (output image with encoded data, use loseless img format for output bmp/png, etc...)");
					System.out.println("4 - random seed(long), something like key, default 4963");					
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
					System.out.println("3 - random seed(long), something like key, default 4963");					
					System.out.println("examples");
					System.out.println("-steg -d image.png - decode data from image into actual directory using seed(key) 4963");					
					System.out.println("-steg -d image.png c:/data/ 12345  - decode data from image into directory c:/data/ using seed(key) 12345");
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
					ONE_TIME_PAD(args[1], args[2], seed);
				}else {					
					System.out.println("Program Arguments");
					System.out.println("1 - input file - mandatory");
					System.out.println("2 - output file - mandatory");
					System.out.println("3 - random seed(long), something like key, default 4963");
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
					GEN_RANDOM_DATA_FILE(args[1], Long.parseLong(args[2]), seed);
				}else {					
					System.out.println("Program Arguments");
					System.out.println("1 - input file - mandatory");
					System.out.println("2 - file size(long) - mandatory");
					System.out.println("3 - random seed(long), something like key, default 4963");
					System.out.println("");
					System.out.println("generate binary file with random data of selected size");
					System.out.println("examples");
					System.out.println("-gen output.bin 1024 		- generate file output.bin of size 1024 with default seed 4963");
					System.out.println("-gen output.bin 1024 13245	- generate file output.bin of size 1024 with seed 12345 (same seed generate same data)");
				}
			}		
			//file generation
			if(args[0].toLowerCase().equals("-del")) {
				if(args.length>=2) {
					if(args.length>=3)
						seed = Long.parseLong(args[2]);
					SECURE_DELETE(args[1], seed);
				}else {					
					System.out.println("Program Arguments");
					System.out.println("1 - input file/folder - mandatory");					
					System.out.println("2 - random seed(long), default 4963");
					System.out.println("");
					System.out.println("secure delete mean rewriting the data instead of common delete in system, when only file is marked as delete and the data stay unchanged");
					System.out.println("function rewrites content and name for file or folder (recursively to all subfolders and files) to random data");
					System.out.println("used Java random, for more secure it is recommended to run function more times with different seed");
					System.out.println("examples");
					System.out.println("-del fileTo.del		- delete file fileTo.del with default seed 4963");
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
			System.out.println("-gen 	- Random data generating");
			System.out.println("-sgen 	- Secure random data generating");
			System.out.println("-freq 	- Frequency analysis of binary file");
			System.out.println("-del 	- Secure delete for files and folders (rewrite data inside files and names to random data)");
			System.out.println("\nrun with argument to see further details");
			System.out.println("");
			System.out.println("-gui 	- run Graphical User Interface program with all mentioned function");
		}
		
		
	}

}