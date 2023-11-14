package Interfaz.Cliente;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Interfaz.Principal.ErrorDisplay;
import Interfaz.Principal.MetodosAuxiliares;
import Interfaz.Principal.Notificacion;
import Interfaz.Principal.Principal;
import Interfaz.Principal.Verify;
import RentadoraModelo.Categoria;
import RentadoraModelo.Cliente;
import RentadoraModelo.Reserva;
import RentadoraModelo.Sede;

public class HacerReserva extends JPanel implements MetodosAuxiliares, ActionListener  {

	Principal principal;
	private JComboBox<String> sedes;
	private JComboBox<String> categorias;
	private JButton confirmar;
	private GridBagConstraints gbc;
	static final int textFieldSize = 20;
	static final int YSpace = 5;
	private ArrayList<JTextField> listaCampos;
	private Verify verificador;
	private ErrorDisplay error;
	private Notificacion notify;
	ArrayList<String> campos = new ArrayList<String>();
	
	public HacerReserva(Principal principal) {
		
		this.principal = principal;
		this.listaCampos = new ArrayList<JTextField>();
		this.verificador = new Verify();
		
		setLayout(new GridBagLayout());
		
		this.gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.WEST;
		
		addCampos();
		
		addBoxes();
		
		confirmar = new JButton("Confirmar");
		formatButton(confirmar);
		confirmar.setActionCommand("CONFIRMAR");
		confirmar.addActionListener(this);
		add(confirmar,gbc);

	}
	
	private void addBoxes() {
		
		ArrayList<String> todasSedes = principal.cargaArchivos.cargarSedes();
		sedes = new JComboBox<>();
		for (String sede: todasSedes) {			
        sedes.addItem(sede);
		}
		add(sedes,gbc);
		
		ArrayList<String> todasCategorias = principal.cargaArchivos.cargarCategoria();
        
		categorias = new JComboBox<>();
		for (String categoria: todasCategorias) {
        categorias.addItem(categoria);
		}
		add(categorias,gbc);
		
	}

	private void addCampos() {
		campos.add("Día recogida (dd/mm/YYYY)");
		campos.add("Hora Recogida (HH:MM)");
		campos.add("Día entrega (dd/mm/YYYY)");
		campos.add("Hora Entrega (HH:MM)");

        
        for (String mensaje : campos) {
        	JTextField campo = new JTextField(textFieldSize);
            ponerTextitoGris(campo, mensaje);
            addSpace(YSpace);
            listaCampos.add(campo);
            add(campo,gbc);
		}
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		String grito = e.getActionCommand();
		if (grito.equals("CONFIRMAR")) {	
			
			String sede = (String) sedes.getSelectedItem();
			String categoria = (String) categorias.getSelectedItem();
			
			ArrayList<String> info = new ArrayList<String>();
			for (JTextField textCampo: listaCampos) {
				String texto = (String) textCampo.getText();
				if (campos.contains(texto)) {
					info.add("");
				}
				else {
					info.add(texto);
				}
			}
			boolean verifyLleno = verificador.verifyLleno(info);
			if (verifyLleno == true) {
			Properties pLogin = principal.cargaArchivos.cargarLogin();
			boolean verifyTarjeta = verificador.verifyTarjetaBloqueada(pLogin, principal.usernameActual);
			if (verifyTarjeta == false) {
				error = new ErrorDisplay("SU TARJETA SIGUE BLOQUEADA, ENTREGUE EL CARRO ANTES DE HACER OTRA RESERVA");
			}
			else {	
				
				boolean verify = verificador.verifyFechaValida(principal.sedePresente,info.get(1),info.get(3));
				if (verify == true) {
				Cliente cliente = (Cliente) principal.usuarioActual;
				
				Categoria categoriaEscogida = new Categoria(categoria);
				categoriaEscogida.setMejorCategoria(categoriaEscogida);
				
				Reserva reservaSeleccionado = cliente.iniciarReserva(info.get(0), info.get(1),info.get(2),info.get(3),
										principal.sedePresente, categoriaEscogida,sede);
				
				if (reservaSeleccionado.getCarroEscogido() == null) {
					error = new ErrorDisplay("NO SE ENCONTRO VEHICULOS ACORDES A SU BUSQUEDA,INTENTE MÁS TARDE");
				}
				else {
					reservaSeleccionado.setTarifa();
					principal.cargaArchivos.cargarTarReserva(reservaSeleccionado, cliente);
					String mensaje = "";
					mensaje += "=======================CARACTERISTICAS RESERVA ============================\n";
					mensaje += "El carro que le fue asignado tiene las siguientes propiedades: \n";
					mensaje += "Si no coincide con su categoria es que se agotaron, y le dimos una superior \n";
					mensaje +="=========================================================================== \n";
					mensaje +="placa: " + reservaSeleccionado.getCarroEscogido().getPlaca()+"\n";
					mensaje +="marca: " + reservaSeleccionado.getCarroEscogido().getMarca()+"\n";
					mensaje +="modelo: " + reservaSeleccionado.getCarroEscogido().getModelo()+"\n";
					mensaje +="color: " + reservaSeleccionado.getCarroEscogido().getColor()+"\n";
					mensaje +="transmision: " + reservaSeleccionado.getCarroEscogido().getTipoDeTransmision()+"\n";
					mensaje +="combustible: " + reservaSeleccionado.getCarroEscogido().getCombustible()+"\n";
					mensaje +="categoria: " + reservaSeleccionado.getCarroEscogido().getCategoria().getNombre()+"\n";
					mensaje +="=========================================================================== \n";
					mensaje +="Ahora le cobraremos el 30% de una tarifa estimada \n";
					mensaje +="Valor estimado total: " + reservaSeleccionado.getTarifa().getPrecio()+"\n";
					mensaje +="Se le cobrarará: " + 0.3*reservaSeleccionado.getTarifa().getPrecio()+"\n";
					mensaje +="Transaccion Hecha Satisfactoriamente \n";
					mensaje +="Reserva Guardada Satisfactoriamente \n";
					mensaje +="=========================================================================== \n";
					mensaje +="EL ID DE SU RESERVA ES: " + reservaSeleccionado.getId()+cliente.getNombre()+"\n";
					mensaje +="RECUERDE EL ID PORQUE SINO NO PODRA FINALIZAR EL ALQUILER \n";
					mensaje +="===========================================================================";
					notify = new Notificacion(mensaje);
				}
				
				principal.cambiarPanel("menuCliente");
				}
				else {
					error = new ErrorDisplay("LA HORA NO ESTÁ DENTRO DEL HORARIO DE ATENCIÓN, PORFAVOR ESCOGERLA DE NUEVO");
				}
			}
			}
			else {
				error = new ErrorDisplay("LLENE TODO LOS CAMPOS");
			}
			
		}
		
	}
	
	private void addSpace(int Yspace) {
		add(Box.createRigidArea(new Dimension(0, Yspace)), gbc);
	}

}