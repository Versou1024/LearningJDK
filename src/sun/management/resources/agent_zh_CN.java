package sun.management.resources;

import java.util.ListResourceBundle;

public final class agent_zh_CN extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "agent.err.access.file.not.readable", "无法读取访问文件" }, { "agent.err.access.file.notfound", "找不到访问文件" }, { "agent.err.access.file.notset", "未指定访问文件，但 com.sun.management.jmxremote.authenticate=true" }, { "agent.err.access.file.read.failed", "读取访问文件失败" }, { "agent.err.acl.file.access.notrestricted", "必须限制口令文件读取访问" }, { "agent.err.acl.file.not.readable", "无法读取 SNMP ACL 文件" }, { "agent.err.acl.file.notfound", "找不到 SNMP ACL 文件" }, { "agent.err.acl.file.notset", "未指定 SNMP ACL 文件，但 com.sun.management.snmp.acl=true" }, { "agent.err.acl.file.read.failed", "读取 SNMP ACL 文件失败" }, { "agent.err.agentclass.access.denied", "拒绝访问 premain(String)" }, { "agent.err.agentclass.failed", "管理代理类失败 " }, { "agent.err.agentclass.notfound", "找不到管理代理类" }, { "agent.err.configfile.access.denied", "拒绝访问配置文件" }, { "agent.err.configfile.closed.failed", "关闭配置文件失败" }, { "agent.err.configfile.failed", "读取配置文件失败" }, { "agent.err.configfile.notfound", "找不到配置文件" }, { "agent.err.connector.server.io.error", "JMX 连接器服务器通信错误" }, { "agent.err.error", "错误" }, { "agent.err.exception", "代理抛出异常 " }, { "agent.err.exportaddress.failed", "将 JMX 连接器地址导出到测试设备缓冲区失败" }, { "agent.err.file.access.not.restricted", "必须限制文件读取权限" }, { "agent.err.file.not.found", "找不到文件" }, { "agent.err.file.not.readable", "无法读取文件" }, { "agent.err.file.not.set", "未指定文件" }, { "agent.err.file.read.failed", "读取文件失败" }, { "agent.err.invalid.agentclass", "com.sun.management.agent.class 属性值无效" }, { "agent.err.invalid.jmxremote.port", "com.sun.management.jmxremote.port 编号无效" }, { "agent.err.invalid.option", "指定的选项无效" }, { "agent.err.invalid.snmp.port", "com.sun.management.snmp.port 编号无效" }, { "agent.err.invalid.snmp.trap.port", "com.sun.management.snmp.trap 编号无效" }, { "agent.err.password.file.access.notrestricted", "必须限制口令文件读取访问" }, { "agent.err.password.file.not.readable", "无法读取口令文件" }, { "agent.err.password.file.notfound", "找不到口令文件" }, { "agent.err.password.file.notset", "未指定口令文件，但 com.sun.management.jmxremote.authenticate=true" }, { "agent.err.password.file.read.failed", "读取口令文件失败" }, { "agent.err.premain.notfound", "代理类中不存在 premain(String)" }, { "agent.err.snmp.adaptor.start.failed", "无法启动带有地址的 SNMP 适配器" }, { "agent.err.snmp.mib.init.failed", "无法初始化带有错误的 SNMP MIB" }, { "agent.err.unknown.snmp.interface", "未知 SNMP 接口" }, { "agent.err.warning", "警告" }, { "jmxremote.AdaptorBootstrap.getTargetList.adding", "正在添加目标：{0}" }, { "jmxremote.AdaptorBootstrap.getTargetList.initialize1", "适配器就绪。" }, { "jmxremote.AdaptorBootstrap.getTargetList.initialize2", "位于 {0}:{1} 的 SNMP 适配器就绪" }, { "jmxremote.AdaptorBootstrap.getTargetList.processing", "正在处理 ACL" }, { "jmxremote.AdaptorBootstrap.getTargetList.starting", "正在启动适配器服务器：" }, { "jmxremote.AdaptorBootstrap.getTargetList.terminate", "终止 {0}" }, { "jmxremote.ConnectorBootstrap.initialize", "正在启动 JMX 连接器服务器：" }, { "jmxremote.ConnectorBootstrap.initialize.file.readonly", "必须限制文件读取权限: {0}" }, { "jmxremote.ConnectorBootstrap.initialize.noAuthentication", "无验证" }, { "jmxremote.ConnectorBootstrap.initialize.password.readonly", "必须限制口令文件读取访问：{0}" }, { "jmxremote.ConnectorBootstrap.initialize.ready", "位于 {0} 的 JMX 连接器就绪" } };
  }
}