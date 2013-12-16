netty-xnio-transport
====================

This is a Netty Transport powered by XNIO. It can be used to either wrap an existing `StreamConnection` and `AcceptingChannel` or to bootstrap a Netty powered server directly. 

To bootstrap a server use it the same way as other transport implementations. The following code snipped shows an example:

```java
    public void run() throws Exception {
        // Configure the server.
        EventLoopGroup group = new XnioEventLoopGroup(4);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
             .childHandler(new HelloServerInitializer())

             .channel(XnioServerSocketChannel.class)
             .option(XnioChannelOption.BALANCING_TOKENS, 1)
             .option(XnioChannelOption.BALANCING_CONNECTIONS, 2)
             .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            Channel ch = b.bind(port).sync().channel();
            ch.closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
```
