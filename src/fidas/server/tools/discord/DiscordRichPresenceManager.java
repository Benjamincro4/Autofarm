/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fidas.server.tools.discord;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

public class DiscordRichPresenceManager {

    private static final String CLIENT_ID = "1304214075982614588"; // Reemplaza con tu Client ID de Discord
    private static DiscordRPC discordRPC;
    private static Thread rpcThread;

    // Método para iniciar Discord Rich Presence
    public static void initialize() {
        DiscordEventHandlers eventHandlers = new DiscordEventHandlers();
        
        // Configura los eventos de Discord (puedes manejar más eventos si lo deseas)
        eventHandlers.ready = user -> System.out.println("Conectado a Discord como: " + user.username);
       	
		// Inicializa el RPC con el Client ID
		DiscordRPC.discordInitialize(CLIENT_ID, eventHandlers, true);

		// Crear un hilo que actualice la presencia constantemente
		rpcThread = new Thread(() -> {
		    try {
		        // Actualiza la presencia cada segundo
		        while (!Thread.currentThread().isInterrupted()) {
		        	DiscordRPC.discordRunCallbacks();
		            Thread.sleep(1000);
		        }
		    } catch (InterruptedException e) {
		        System.err.println("Error en el hilo de Discord RPC: " + e.getMessage());
		    }
		});
		
		rpcThread.start();

		// Establecer la presencia inicial
		updatePresence("Jugando a L2Fidas.com", "Juega en nuestro servidor H5");

		System.out.println("Discord Rich Presence activado.");
    }

    // Método para actualizar la presencia
    public static void updatePresence(String state, String details) {
        DiscordRichPresence richPresence = new DiscordRichPresence();
        richPresence.state = state;
        richPresence.details = details;

        // Definir la imagen grande (opcional)
        richPresence.largeImageKey = null;  // Reemplaza con tu imagen personalizada si la tienes
        richPresence.largeImageText = "L2Fidas";     // Texto que aparece al pasar el ratón sobre la imagen

        DiscordRPC.discordUpdatePresence(richPresence);
    }

    // Método para apagar y limpiar el RPC
    public static void shutdown() {
        if (discordRPC != null) {
        	DiscordRPC.discordShutdown();
        }

        if (rpcThread != null && rpcThread.isAlive()) {
            rpcThread.interrupt();
        }

        System.out.println("Discord Rich Presence apagado.");
    }
}