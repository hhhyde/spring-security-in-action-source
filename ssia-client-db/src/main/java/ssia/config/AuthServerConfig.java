package ssia.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

import javax.sql.DataSource;

@Configuration
@EnableAuthorizationServer
public class AuthServerConfig
        extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    DataSource dataSource;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//        DataSource dataSource = DataSourceBuilder.create()
//                .driverClassName("com.mysql.jdbc.Driver")
//                .url("jdbc:mysql://localhost:3306/oauth2_demo")
//                .username("oauth2_demo")
//                .password("oauth2_demo").build();
        clients.jdbc(dataSource);
//        clients.jdbc(DataSourceBuilder.create()
//                .driverClassName("com.mysql.jdbc.Driver")
//        )       .u
        //        clients.inMemory()
//                .withClient("client")
//                .secret("secret")
//                .authorizedGrantTypes("password", "refresh_token")
//                .scopes("read")
//        .and()
//                .withClient("resourceserver")
//                .secret("resourceserversecret");

    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authenticationManager(authenticationManager);
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.checkTokenAccess("isAuthenticated()");
    }
}
