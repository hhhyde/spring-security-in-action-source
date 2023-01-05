package ssia;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

    @Autowired
    private MockMvc mvc;
    @Autowired
    DataSource dataSource;

    @Test
    @DisplayName("Test access_token is obtained using valid user and client")
    public void testAccessTokenIsObtainedUsingValidUserAndClient() throws Exception {
        // 运行时新增一个客户端
        Connection connection = dataSource.getConnection();
        PreparedStatement prepareStatement = connection.prepareStatement("INSERT INTO `oauth_client_details` (`client_id`, `client_secret`,  `scope`, `authorized_grant_types`) VALUES (?, ?, ?, ?)");
        prepareStatement.setString(1, "client1");
        prepareStatement.setString(2, "secret");
        prepareStatement.setString(3, "read");
        prepareStatement.setString(4, "password,refresh_token");
        int rows = prepareStatement.executeUpdate();
        System.out.println("<新增>客户端 client1");


        mvc.perform(
                post("/oauth/token")
                        .with(httpBasic("client1", "secret"))
                        .queryParam("grant_type", "password")
                        .queryParam("username", "john")
                        .queryParam("password", "12345")
                        .queryParam("scope", "read")
        )
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(status().isOk())
                .andDo(print());

        PreparedStatement ps = connection.prepareStatement("delete from `oauth_client_details` where `client_id`= ?");
        ps.setString(1, "client1");
        int rows1 = ps.executeUpdate();
        System.out.println("<删除>客户端 client1");
        connection.close();


    }

    @Test
    @DisplayName("Test check_token endpoint returns the access token details")
    public void testCheckTokenEndpoint() throws Exception {
        // 运行时新增一个客户端
        Connection connection = dataSource.getConnection();
        PreparedStatement prepareStatement = connection.prepareStatement("INSERT INTO `oauth_client_details` (`client_id`, `client_secret`,  `scope`, `authorized_grant_types`) VALUES (?, ?, ?, ?)");
        prepareStatement.setString(1, "client1");
        prepareStatement.setString(2, "secret");
        prepareStatement.setString(3, "read");
        prepareStatement.setString(4, "password,refresh_token");
        prepareStatement.executeUpdate();
        PreparedStatement prepareStatement1 = connection.prepareStatement("INSERT INTO `oauth_client_details` (`client_id`, `client_secret`,  `scope`, `authorized_grant_types`) VALUES (?, ?, ?, ?)");
        prepareStatement1.setString(1, "resourceserver");
        prepareStatement1.setString(2, "resourceserversecret");
        prepareStatement1.setString(3, "read");
        prepareStatement1.setString(4, "password,refresh_token");
        prepareStatement1.executeUpdate();
        System.out.println("<新增>客户端 client1,resourceserver");

        // 测试token
        String content =
                mvc.perform(
                                post("/oauth/token")
                                        .with(httpBasic("client1", "secret"))
                                        .queryParam("grant_type", "password")
                                        .queryParam("username", "john")
                                        .queryParam("password", "12345")
                                        .queryParam("scope", "read")
                        )
                        .andDo(print())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        Map<String,String> map = mapper.readValue(content, Map.class);

        try {

            mvc.perform(
                            get("/oauth/check_token")
                                    .with(httpBasic("resourceserver", "resourceserversecret"))
                                    .queryParam("token", map.get("access_token"))
                    )
                    .andDo(print())
                    .andExpect(jsonPath("$.user_name").value("john"))
                    .andExpect(jsonPath("$.client_id").value("client1"))
                    .andExpect(status().isOk());

        }finally {
            // 删除测试数据
            PreparedStatement ps = connection.prepareStatement("delete from `oauth_client_details` where `client_id`= ?");
            ps.setString(1, "client1");
            ps.executeUpdate();
            PreparedStatement ps1 = connection.prepareStatement("delete from `oauth_client_details` where `client_id`= ?");
            ps1.setString(1, "resourceserver");
            ps1.executeUpdate();
            System.out.println("<删除>客户端 client1,resourceserver");
            connection.close();
        }
    }
}
