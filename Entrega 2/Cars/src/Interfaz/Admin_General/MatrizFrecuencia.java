package Interfaz.Admin_General;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import Interfaz.Principal.MetodosAuxiliares;
import Interfaz.Principal.Principal;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;

public class MatrizFrecuencia extends JPanel implements MetodosAuxiliares, ActionListener{

	Principal principal;
	
	private int year;
	
	private GridBagConstraints gbc;
	
	public static final int alto = 190;
	
	public static final int ancho = 1500;
	
	private static final int margenX = 70;
	
	private static final int margenY = 30;
	
	private int carrosTotales;
	
	public static String sedeActual;
		
	private int[][] cuadricula;
	
	public MatrizFrecuencia(Principal principal) {	
		
		this.principal = principal;
		
		this.year = 2023;
		
		this.carrosTotales = 0;
		
		this.gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
				
		crearCuadricula();
		
		pintarCuadricula();
						
		setVisible(true);
	}
	
	public void crearCuadricula()
	
	{
		cuadricula = new int[7][weeksInAYear()];
		for (int i = 0; i < cuadricula.length; i++)
			for (int ii = 0; ii < cuadricula[0].length; ii++)
			{
				cuadricula[i][ii] = 0;
			}
	}
	
	public int weeksInAYear() {
		
		WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int lastWeekNumber = 1;
        int i = 0;
        while (lastWeekNumber == 1) {
            lastWeekNumber = LocalDate.of(year, 12, 31 - i).get(weekFields.weekOfWeekBasedYear());
            i++;
        }
		
        return lastWeekNumber+1;
	}
	
	public void pintarCuadricula() {
				
		WeekFields weekFields = WeekFields.of(Locale.getDefault());
		
		Set<Entry<Object, Object>> entrySet = principal.cargaArchivos.cargarReserva().entrySet();
		
		
        for (Entry<Object, Object> entry : entrySet) {
            String valores = (String) entry.getValue();
            String[] datos = valores.split(";");
            
            String sede = datos[5];
            
            if (!sede.equals(sedeActual)) {
            	continue;
            }
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            LocalDate fechaInicial = LocalDate.parse(datos[0], dateFormatter);
            LocalDate fechaFinal = LocalDate.parse(datos[2], dateFormatter);
            
            LocalDate currentDate = fechaInicial;
            while (!currentDate.isAfter(fechaFinal)) {
            	
            	int semana = currentDate.get(weekFields.weekOfWeekBasedYear())- 1;
            	
            	int diaDeLaSemana = currentDate.getDayOfWeek().getValue()%7;
            	
            	cuadricula[diaDeLaSemana][semana]++;
            	
            	currentDate = currentDate.plus(1, ChronoUnit.DAYS);
                
            }
        }
		
		
	}
	
	
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		Color cutePurple = new Color(207, 159, 255);
		Color cuteYellow = new Color(255,253,141);
		
		int gridSize = alto/cuadricula.length;
		
		//Añadir dias de la semana
		String[] daysOfWeek = {"Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
		for (int i = 0; i < cuadricula.length; i++) {
	        
	        g.drawString(daysOfWeek[i], 10 ,i*gridSize + margenY + 15);
			
		}
		
		//Añadir meses
		for (Month month : Month.values()) {

            LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
            WeekFields weekFields = WeekFields.of(Locale.getDefault());

            int firstWeekNumber = firstDayOfMonth.get(weekFields.weekOfWeekBasedYear())- 1;
            
            String monthName = month.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            String capitalizedMonthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
            
            g.drawString(capitalizedMonthName, firstWeekNumber*gridSize + margenX + 0, 15);
        }
		
		
		//Añadir cuadritos
		for (int i = 0; i < cuadricula.length; i++)
			for (int ii = 0; ii < cuadricula[0].length; ii++) {
				
				int x = ii*gridSize + margenX;
				int y = i*gridSize + margenY;
				
				int frecuencia = cuadricula[i][ii];
				if (frecuencia == 0) {
					
					 Point2D start = new Point2D.Float(x, y); // Starting point of the gradient
					 Point2D end = new Point2D.Float((float)start.getX()+gridSize, (float)start.getY()+gridSize);
					 GradientPaint gradient = new GradientPaint(start, cutePurple, end, cuteYellow);
					 g2d.setPaint(gradient);
					
				} else if (frecuencia < 2){
					Point2D start = new Point2D.Float(x, y); // Starting point of the gradient
					Point2D end = new Point2D.Float((float)start.getX()+gridSize, (float)start.getY()+gridSize);
					GradientPaint gradient = new GradientPaint(start, Color.BLACK, end, Color.WHITE);
					g2d.setPaint(gradient);
				} else {
					Point2D start = new Point2D.Float(x, y); // Starting point of the gradient
					Point2D end = new Point2D.Float((float)start.getX()+gridSize, (float)start.getY()+gridSize);
					GradientPaint gradient = new GradientPaint(start, Color.RED, end, Color.BLUE);
					g2d.setPaint(gradient);
				}
				
				RoundRectangle2D.Double rectangle = new  RoundRectangle2D.Double(x,y,gridSize,gridSize,20,20);
				g2d.fill(rectangle);
				g2d.draw(rectangle);
			} 
		
		//Añadir notacion 100%
		int x = 3*gridSize + margenX;
		int y = cuadricula.length*gridSize + margenY + 20;
		
		Point2D start = new Point2D.Float(x, y); // Starting point of the gradient
		Point2D end = new Point2D.Float((float)start.getX()+gridSize, (float)start.getY()+gridSize);
		GradientPaint gradient = new GradientPaint(start, cutePurple, end, cuteYellow);
		g2d.setPaint(gradient);
		
		RoundRectangle2D.Double rectangle = new  RoundRectangle2D.Double(x,y,gridSize,gridSize,20,20);
		g2d.fill(rectangle);
		g2d.draw(rectangle);
		
		setTextFont(g);
		g.drawString("100%",x+gridSize+5,y + gridSize -5);
		
		//Añadir notacion 99-50%
		x = 7*gridSize + margenX;
		y = cuadricula.length*gridSize + margenY + 20;
		
		start = new Point2D.Float(x, y); // Starting point of the gradient
		end = new Point2D.Float((float)start.getX()+gridSize, (float)start.getY()+gridSize);
		gradient = new GradientPaint(start, Color.BLACK, end, Color.WHITE);
		g2d.setPaint(gradient);
		
		rectangle = new  RoundRectangle2D.Double(x,y,gridSize,gridSize,20,20);
		g2d.fill(rectangle);
		g2d.draw(rectangle);
		
		setTextFont(g);
		g.drawString("99%-50%",x+gridSize+5,y + gridSize -5);
		
		//Añadir notacion 100%
		x = 11*gridSize + margenX;
		y = cuadricula.length*gridSize + margenY + 20;
		
		start = new Point2D.Float(x, y); // Starting point of the gradient
		end = new Point2D.Float((float)start.getX()+gridSize, (float)start.getY()+gridSize);
		gradient = new GradientPaint(start, Color.RED, end, Color.BLUE);
		g2d.setPaint(gradient);
		
		rectangle = new  RoundRectangle2D.Double(x,y,gridSize,gridSize,20,20);
		g2d.fill(rectangle);
		g2d.draw(rectangle);
		
		setTextFont(g);
		g.drawString("49%-0%",x+gridSize+5,y + gridSize -5);
	}
	
	@Override
    public Dimension getPreferredSize() {
        // Set the preferred width and height of your panel here
        return new Dimension(ancho+50, alto+50); // Replace with your desired dimensions
    }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		String grito = e.getActionCommand();
		
		principal.cambiarPanel(grito);
		
	}
	
	private void addSpace(int Yspace) {
		add(Box.createRigidArea(new Dimension(0, Yspace)), gbc);
	}

}
