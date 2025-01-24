package fileEncoder;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

/**
 *
 * @author kvaderlipa
 */
public class ImageByteStreamWriter{
	private Random r;
    private int bit;
    private int position;
    private int[] matrix = null;    
    private int BGR; // 0 - blue, 1 - green, 2 - red
    private int width;
    private int height;
        
    private int[] pixelMix;
    
    
    private final BufferedImage img;
        
    public int getBitSize(){
        return bit;
    }
        
    private int trans(int x, int y){    	
    	return x+y*width;
    }    
    
    public ImageByteStreamWriter(BufferedImage img, Random r) {
    	this.width = img.getWidth();
    	this.height = img.getHeight();
    	//load whole matrix
    	matrix = new int[width*height];        
    	img.getRGB(0, 0, width, height, matrix, 0, width);
        
    	this.img = img;
        this.r = r;
        bit = 0;
        position = 0;
        BGR = 0;
                
        pixelMix = new int[width*height];
        for (int i = 0; i < pixelMix.length; i++)
        	pixelMix[i] = i;
        shuffle(pixelMix, r);        
    }
    
   // este vyskusat na zrychlenie, ze budem mat maticu farieb a az potom ich nahram do obrazka, lebo takto po bitoch meni byty v img objekte asi bude pomale,,, to by mohlo ist
    int pom, vec;
    public void write(int c) throws IOException{        
        c ^= r.nextInt(); // One time PAD to randomize data
        
        for (int i = 0; i < 8; i++) {
            if(BGR==3){               //tocime RGB 
                if(++position==width*height){ //tocime position
                    if(++bit==8)                //akpresvihneme bity koniec
                        throw new IOException("Image full, no space in image.");
                    position = 0;                    
                }            
                BGR = 0;
            }
                        
            vec = 1;              
            vec <<= bit;
            vec <<= BGR*8; // najdeme vektor, to jest v inte 1 iba tam ktory bit riesime
            
            pom = c;
            pom >>= i;
            pom &= 1;            //zistime ci ideme zapisovat 0 alebo 1
            if(pom==1)          //ak 1, tak or zapise 1 na prislusne miesto
            	matrix[trans(pixelMix[position]%width, pixelMix[position]/width)] |= vec;
            else
            	matrix[trans(pixelMix[position]%width, pixelMix[position]/width)] &= ~vec;  // ak 0, tak & nam na inverznom vektore urobi z bitu 0 HAHH, genialne, ale myslim si, ze to isto pojde aj lahsie HAHHA

            BGR++;            
        }
    }
    
    public void trans2img(){
    	img.setRGB(0, 0, width, height, matrix, 0, width);
    }
    
    //shuffle all pixels to positions
    public static void shuffle(int[] pole, Random r){
        int index;
        int pom;
        for (int i = pole.length; i > 1; i--){
        	//swap
        	pom = pole[i-1];
        	index = r.nextInt(i);
        	pole[i-1] = pole[index];
        	pole[index] = pom;
        }
    }
    
    public static void main(String[] args) throws IOException{
        int len  = 100000000;
    	/*
        ArrayList<Integer> pole = new ArrayList<>(len);
        for (int i = 0; i < len; i++)
            pole.add(i);
        Collections.shuffle(pole);        
        for (int i = 0; i < 100; i++)
            System.out.println(pole.get(i));
        */
        int[] pole = new int[len];
        for (int i = 0; i < pole.length; i++)
        	pole[i] = i;
        
        Random r = new Random(222);
        shuffle(pole, r);
        for (int i = 0; i < 100; i++)
            System.out.println(pole[i]);
        
        /*
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("image640x480.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        ImageByteStreamWriter ibsw = new ImageByteStreamWriter(img);
        String message = "0123456789";        
        for (int j = 0; j < 3*640*48/2; j++) 
            for (int i = 0; i < message.length(); i++) {            
                ibsw.write((int)message.charAt(i));
            }
        //ibsw.write(1);
        try {
            ImageIO.write(img, "png", new File("encImage.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
    
}
