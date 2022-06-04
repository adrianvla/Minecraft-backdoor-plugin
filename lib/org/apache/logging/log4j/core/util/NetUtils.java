// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.util;

import org.apache.logging.log4j.status.StatusLogger;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.io.File;
import java.net.URL;
import java.net.URI;
import java.util.Arrays;
import java.util.Enumeration;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.net.NetworkInterface;
import java.net.InetAddress;
import org.apache.logging.log4j.Logger;

public final class NetUtils
{
    private static final Logger LOGGER;
    private static final String UNKNOWN_LOCALHOST = "UNKNOWN_LOCALHOST";
    
    private NetUtils() {
    }
    
    public static String getLocalHostname() {
        try {
            final InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostName();
        }
        catch (UnknownHostException uhe) {
            try {
                final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    final NetworkInterface nic = interfaces.nextElement();
                    final Enumeration<InetAddress> addresses = nic.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        final InetAddress address = addresses.nextElement();
                        if (!address.isLoopbackAddress()) {
                            final String hostname = address.getHostName();
                            if (hostname != null) {
                                return hostname;
                            }
                            continue;
                        }
                    }
                }
            }
            catch (SocketException se) {
                NetUtils.LOGGER.error("Could not determine local host name", uhe);
                return "UNKNOWN_LOCALHOST";
            }
            NetUtils.LOGGER.error("Could not determine local host name", uhe);
            return "UNKNOWN_LOCALHOST";
        }
    }
    
    public static byte[] getMacAddress() {
        byte[] mac = null;
        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            try {
                final NetworkInterface localInterface = NetworkInterface.getByInetAddress(localHost);
                if (isUpAndNotLoopback(localInterface)) {
                    mac = localInterface.getHardwareAddress();
                }
                if (mac == null) {
                    NetworkInterface nic;
                    for (Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces(); networkInterfaces.hasMoreElements() && mac == null; mac = nic.getHardwareAddress()) {
                        nic = networkInterfaces.nextElement();
                        if (isUpAndNotLoopback(nic)) {}
                    }
                }
            }
            catch (SocketException e) {
                NetUtils.LOGGER.catching(e);
            }
            if (mac == null || mac.length == 0) {
                final byte[] address = localHost.getAddress();
                mac = Arrays.copyOf(address, 6);
            }
        }
        catch (UnknownHostException ex) {}
        return mac;
    }
    
    public static String getMacAddressString() {
        final byte[] macAddr = getMacAddress();
        if (macAddr != null && macAddr.length > 0) {
            final StringBuilder sb = new StringBuilder(String.format("%02x", macAddr[0]));
            for (int i = 1; i < macAddr.length; ++i) {
                sb.append(":").append(String.format("%02x", macAddr[i]));
            }
            return sb.toString();
        }
        return null;
    }
    
    private static boolean isUpAndNotLoopback(final NetworkInterface ni) throws SocketException {
        return ni != null && !ni.isLoopback() && ni.isUp();
    }
    
    public static URI toURI(final String path) {
        try {
            return new URI(path);
        }
        catch (URISyntaxException e) {
            try {
                final URL url = new URL(path);
                return new URI(url.getProtocol(), url.getHost(), url.getPath(), null);
            }
            catch (MalformedURLException | URISyntaxException ex2) {
                final Exception ex;
                final Exception nestedEx = ex;
                return new File(path).toURI();
            }
        }
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
    }
}
