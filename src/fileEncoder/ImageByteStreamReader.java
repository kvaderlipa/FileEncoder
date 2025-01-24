package fileEncoder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 *
 * @author kvaderlipa
 */
public class ImageByteStreamReader{
	private Random r;
    private int bit;
    private int position;
    private int BGR; // 0 - blue, 1 - green, 2 - red
    private int[] matrix = null;
    private int[] pixelMix;    
    private int width;
    private int height;
        
    public ImageByteStreamReader(BufferedImage img, Random r) {
    	this.width = img.getWidth();
    	this.height = img.getHeight();
    	//load whole matrix
    	matrix = new int[width*height];        
    	img.getRGB(0, 0, width, height, matrix, 0, width);
    	
        this.r = r;
        bit = 0;
        position = 0;
        BGR = 0;
                
        pixelMix = new int[width*height];
        for (int i = 0; i < pixelMix.length; i++)
        	pixelMix[i] = i;
        ImageByteStreamWriter.shuffle(pixelMix, r);
    }
        
    int color;
    public int read(){
        int ret = 0;        
        
        for (int i = 0; i < 8; i++) {
            if(BGR==3){               //tocime RGB 
                if(++position==width*height){ //tocime position
                    if(++bit==8)                //akpresvihneme bity koniec
                        return -1;
                    position = 0;                    
                }            
                BGR = 0;
            }
            
            color = matrix[trans(pixelMix[position]%width, pixelMix[position]/width)];
                        
            color >>= BGR*8;    //posunieme o RGB
            color >>= bit;      //posunieme o bit
            color &= 1;// zistime ci je na bit 0 alebo 1
            
            color <<= i; // posunieme na prislusne miesto vo vyslednom byte
            ret |= color;         
            
            BGR++;            
        }
        
        return (ret^r.nextInt())&255;
    }
    
    private int trans(int x, int y){    	
    	return x+y*width;
    }
    
    public static void main(String[] args){
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("encImage.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        ImageByteStreamReader ibsr = new ImageByteStreamReader(img, new Random(FileEncoder.DEFAULT_SEED));
        int r, count = 0;
        while((r = ibsr.read())!=-1){
            System.out.print((char)r);
            if(++count==200)
                break;
        }
        
        
    }
    
}
