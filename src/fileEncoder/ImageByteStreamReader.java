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
    
    /* old mixing method
    private ArrayList<Integer> transX;
    private ArrayList<Integer> transY;
    */
    private int[] pixelMix;

    private BufferedImage img;
    
    public ImageByteStreamReader(BufferedImage img) {
        this(img, 4963);
    }
    
    public ImageByteStreamReader(BufferedImage img, long seed) {
        this.img = img;         
        bit = 0;
        position = 0;
        BGR = 0;
        
        r = new Random(seed);
        pixelMix = new int[img.getWidth()*img.getHeight()];
        for (int i = 0; i < pixelMix.length; i++)
        	pixelMix[i] = i;
        ImageByteStreamWriter.shuffle(pixelMix, r);
        /*
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
    
    
    public int read(){
        int ret = 0;
        int color;
        
        for (int i = 0; i < 8; i++) {
            if(BGR==3){               //tocime RGB 
                if(++position==img.getWidth()*img.getHeight()){ //tocime position
                    if(++bit==8)                //akpresvihneme bity koniec
                        return -1;
                    position = 0;                    
                }            
                BGR = 0;
            }
            
            /* old mixing
            color = img.getRGB(transX.get(position%img.getWidth()), transY.get(position/img.getWidth()));
            */
            color = img.getRGB(pixelMix[position]%img.getWidth(), pixelMix[position]/img.getWidth());
                        
            color >>= BGR*8;    //posunieme o RGB
            color >>= bit;      //posunieme o bit
            color &= 1;// zistime ci je na bit 0 alebo 1
            
            color <<= i; // posunieme na prislusne miesto vo vyslednom byte
            ret |= color;         
            
            BGR++;            
        }
        
        return (ret^r.nextInt())&255;
    }
    
    public static void main(String[] args){
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("encImage.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        ImageByteStreamReader ibsr = new ImageByteStreamReader(img);
        int r, count = 0;
        while((r = ibsr.read())!=-1){
            System.out.print((char)r);
            if(++count==200)
                break;
        }
        
        
    }
    
}
