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
    private int BGR; // 0 - blue, 1 - green, 2 - red
    
    /* old method by columns and rows mixing
    private ArrayList<Integer> transX;
    private ArrayList<Integer> transY;
     */
    //new metwhod for all pixel mixing
    private int[] pixelMix;
    
    
    private final BufferedImage img;
    
    public ImageByteStreamWriter(BufferedImage img) {
        this(img, 4963);
    }
    
    public int getBitSize(){
        return bit;
    }
    
    // put random data to all pixels to complete actual bit, to prevent any detection of ending data
    public void bitAlign(){
    	int color;
        int pom, vec;
        int c;
        
        while(true) {
        	c = r.nextInt();
        	
            if(BGR==3){      
                if(++position==img.getWidth()*img.getHeight()){ //tocime position
                    break;              
                }            
                BGR = 0;
            }
            
            /* old method
            color = img.getRGB(transX.get(position%img.getWidth()), transY.get(position/img.getWidth()));
            */
            color = img.getRGB(pixelMix[position]%img.getWidth(), pixelMix[position]/img.getWidth());
            vec = 1;              
            vec <<= bit;
            vec <<= BGR*8; // najdeme vektor, to jest v inte 1 iba tam ktory bit riesime
            
            pom = c;            
            pom &= 1;            //zistime ci ideme zapisovat 0 alebo 1
            if(pom==1)          //ak 1, tak or zapise 1 na prislusne miesto
                color |= vec;
            else
                color &= ~vec;  // ak 0, tak & nam na inverznom vektore urobi z bitu 0 HAHH, genialne, ale myslim si, ze to isto pojde aj lahsie HAHHA            
            /*old
            img.setRGB(transX.get(position%img.getWidth()), transY.get(position/img.getWidth()), color);
            */
            img.setRGB(pixelMix[position]%img.getWidth(), pixelMix[position]/img.getWidth(), color);
            BGR++;            
        }
    }
        
    public ImageByteStreamWriter(BufferedImage img, long seed) {
        this.img = img;
        bit = 0;
        position = 0;
        BGR = 0;
        
        r = new Random(seed);
        pixelMix = new int[img.getWidth()*img.getHeight()];
        for (int i = 0; i < pixelMix.length; i++)
        	pixelMix[i] = i;
        shuffle(pixelMix, r);
        /* old method
        transX = new ArrayList<>(img.getWidth());
        transY = new ArrayList<>(img.getHeight());
        for (int i = 0; i < img.getWidth(); i++)
            transX.add(i);
        for (int i = 0; i < img.getHeight(); i++)
            transY.add(i);
        Collections.shuffle(transX, r);        
        Collections.shuffle(transY, r);
        */
    }
    
    
    public void write(int c) throws IOException{             
        int color;
        int pom, vec;
        c ^= r.nextInt(); // One time PAD to randomize data
        
        for (int i = 0; i < 8; i++) {
            if(BGR==3){               //tocime RGB 
                if(++position==img.getWidth()*img.getHeight()){ //tocime position
                    if(++bit==8)                //akpresvihneme bity koniec
                        throw new IOException("Image full, no space in image.");
                    position = 0;                    
                }            
                BGR = 0;
            }
            
            /* old method
            color = img.getRGB(transX.get(position%img.getWidth()), transY.get(position/img.getWidth()));
            */
            color = img.getRGB(pixelMix[position]%img.getWidth(), pixelMix[position]/img.getWidth());
                        
            vec = 1;              
            vec <<= bit;
            vec <<= BGR*8; // najdeme vektor, to jest v inte 1 iba tam ktory bit riesime
            
            pom = c;
            pom >>= i;
            pom &= 1;            //zistime ci ideme zapisovat 0 alebo 1
            if(pom==1)          //ak 1, tak or zapise 1 na prislusne miesto
                color |= vec;
            else
                color &= ~vec;  // ak 0, tak & nam na inverznom vektore urobi z bitu 0 HAHH, genialne, ale myslim si, ze to isto pojde aj lahsie HAHHA
            /*old
            img.setRGB(transX.get(position%img.getWidth()), transY.get(position/img.getWidth()), color);
            */
            img.setRGB(pixelMix[position]%img.getWidth(), pixelMix[position]/img.getWidth(), color);
            BGR++;            
        }
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
