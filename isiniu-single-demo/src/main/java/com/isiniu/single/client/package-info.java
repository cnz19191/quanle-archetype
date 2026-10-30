/**
 * 第三方调用放在本包，由 Service 调用。
 *
 * <p>事务方法内禁止发 HTTP、RPC 或 MQ。需要保证一致性时，在事务提交后调用，或使用本地消息表。
 *
 * @author isiniu
 */
package com.isiniu.single.client;
