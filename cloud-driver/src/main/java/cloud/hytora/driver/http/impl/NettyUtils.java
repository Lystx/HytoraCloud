package cloud.hytora.driver.http.impl;

import cloud.hytora.common.collection.NamedThreadFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.concurrent.*;

public class NettyUtils {

	private static final ThreadFactory EVENT_LOOP_THREAD_FACTORY = new NamedThreadFactory("EventLoopGroup");
	private static final RejectedExecutionHandler DEFAULT_REJECTED_HANDLER = new ThreadPoolExecutor.CallerRunsPolicy();

	static {
		// use jdk logger to prevent issues with older slf4j versions
		// like them bundled in spigot 1.8

		try {
			JdkLoggerFactory.class.getDeclaredField("INSTANCE");
			InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE);
		} catch (NoSuchFieldException e) {
			System.out.println("Couldn't set InternalLoggerFactory!");
		}

		// check if the leak detection level is set before overriding it
		// may be useful for debugging of the network
		if (System.getProperty("io.netty.leakDetection.level") == null) {
			ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
		}

		if (Epoll.isAvailable()) {
			CloudDriver.getInstance().getLogger().info("Epoll is available, utilising it..");
		}
	}

	@Nonnull
	@CheckReturnValue
	public static EventLoopGroup newEventLoopGroup() {
		return Epoll.isAvailable() ? new EpollEventLoopGroup(4, getEventLoopThreadFactory())
								   : new NioEventLoopGroup(4, getEventLoopThreadFactory());
	}

	@Nonnull
	@CheckReturnValue
	public static ExecutorService newPacketDispatcher() {
		// a cached pool with a thread idle-lifetime of 30 seconds
		// rejected tasks will be executed on the calling thread (See ThreadPoolExecutor.CallerRunsPolicy)
		return new ThreadPoolExecutor(
			0,
			getThreadAmount(),
			30,
			TimeUnit.SECONDS,
			new SynchronousQueue<>(true),
			new NamedThreadFactory("PacketDispatcher"),
			DEFAULT_REJECTED_HANDLER
		);
	}



	@Nonnull
	@CheckReturnValue
	public static ChannelFactory<? extends Channel> getClientChannelFactory() {
		return Epoll.isAvailable() ? EpollSocketChannel::new
								   : NioSocketChannel::new;
	}

	@Nonnull
	@CheckReturnValue
	public static Class<? extends Channel> getClientChannel() {
		return Epoll.isAvailable() ? EpollSocketChannel.class
				: NioSocketChannel.class;
	}

	@Nonnull
	@CheckReturnValue
	public static Class<? extends ServerChannel> getServerChannel() {
		return Epoll.isAvailable() ? EpollServerSocketChannel.class
				: NioServerSocketChannel.class;
	}

	@Nonnull
	@CheckReturnValue
	public static ChannelFactory<? extends ServerChannel> getServerChannelFactory() {
		return Epoll.isAvailable() ? EpollServerSocketChannel::new
			                       : NioServerSocketChannel::new;
	}

	@Nonnull
	@CheckReturnValue
	public static ThreadFactory getEventLoopThreadFactory() {
		return EVENT_LOOP_THREAD_FACTORY;
	}

	@Nonnull
	public static byte[] asByteArray(@Nonnull ByteBuf buffer) {
		if (buffer.hasArray()) {
			return buffer.array();
		} else {
			byte[] bytes = new byte[buffer.readableBytes()];
			buffer.getBytes(buffer.readerIndex(), bytes);
			return bytes;
		}
	}


	public static int getThreadAmount() {
		return CloudDriver.getInstance().getEnvironment() == DriverEnvironment.SERVICE ? 8 : Runtime.getRuntime().availableProcessors() * 2;
	}

	private NettyUtils() {}
}
