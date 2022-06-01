package com.nttbootcamp.msappmobileyanki;

import com.nttbootcamp.msappmobileyanki.infraestructure.kafkaservices.DebitCardStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class MsAppMobileYankiApplicationTests {


	@Autowired
	private DebitCardStream senddebit;
	@Test
	void contextLoads() {
	}

	@Test
	public void testOpenFeign(){
		//senddebit.createdebitcardyanki("6539-9822-2872-2389");
	//	senddebit.Exist(false);
	}
}



