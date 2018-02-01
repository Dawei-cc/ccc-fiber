package chapter6;

/**
 * @author Fulai Zhang
 * @since 2018/2/1.
 */
public class TestClustedEventBus {
//1, cluster.xml配置
//            <tcp-ip enabled="true">
//    <member>192.168.140.238:5701</member>
//                <member>192.168.140.159:5701</member>
//                <member>192.168.140.104:5701</member>
//                  </tcp-ip>
//    其他的都可以是false
//2,多个主机间通过EventBus连接时，必须VertxOptions::setClusterHost("self-ip-address")，否则只能单机集群
//3,主机间consumer的EventBus地址可以完全不同
//4,EventBus::send 使用round-robin算法选择集群中某个包含address的主机进行发送
//5,EventBus::publish 发送给集群中包含address的所有主机，收不到响应
//6,通过EventBus::registerCodec来发送POJO
}
