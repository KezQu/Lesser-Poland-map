package pl.edu.agh.fis.bd2;

import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.Serializable;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class providing mapping of user credentials from MSSQL Server database to spring boot application
 */
public class UserData implements Serializable {
	private String email;
	private String password;
	private List<String> roles;


	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getRoles() {
		StringBuilder rolesBuilder = new StringBuilder();
		for(String role : roles){
			rolesBuilder.append(role);
			if(!role.equals(roles.getLast()))
				rolesBuilder.append(",");
		}
		return rolesBuilder.toString();
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setRoles(String[] roles) {
		this.roles = Arrays.asList(roles);
	}

	/**
	 * Extractor providing mechanims to retreive user data from MSSQL Server query
	 * @return Extractor
	 */
	public static ResultSetExtractor<List<UserData>> GetExtractor(){
		return (ResultSet rs) ->{
			List<UserData> userList = new ArrayList<>();
			while(rs.next()){
				userList.add(new UserData());
				userList.getLast().email = rs.getNString(1);
				userList.getLast().password = rs.getNString(2);
				userList.getLast().roles = Arrays.asList(rs.getNString(3).split(";"));
			}
			return userList;
		};
	}
}
