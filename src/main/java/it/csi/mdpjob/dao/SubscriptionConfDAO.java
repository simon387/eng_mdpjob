package it.csi.mdpjob.dao;

import it.csi.mdpjob.dao.sm.GenericObjectArrayStatementMapper;
import it.csi.mdpjob.dto.SubscriptionConfig;
import it.csi.mdpjob.enumeration.SubscriptionConfigCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;


// mi adeguo all'architettura del progetto, non sarebbe un DAO e non si dovrebbe fare cosi'
public class SubscriptionConfDAO extends BaseDAO<SubscriptionConfig> {

	private static final Logger log = LoggerFactory.getLogger ( SubscriptionConfDAO.class );

	public SubscriptionConfDAO ( SubscriptionConfigCodeEnum code ) {
		setStatementMapper ( new GenericObjectArrayStatementMapper ( code.getCodice () ) );
		setResultSetExtractor ( new EstraiSubscriptionConfigExtractor () );
	}

	@Override
	public String componiQuery () {
		return "select * " +
						"from subscription_config " +
						"where codice = ? " +
						"  and (data_inizio_validita is null or data_inizio_validita <= current_timestamp) " +
						"  and (data_fine_validita is null or data_fine_validita >= current_timestamp) " +
						"limit 1";
	}

	static class EstraiSubscriptionConfigExtractor implements ResultSetExtractor<SubscriptionConfig> {

		public SubscriptionConfig extractData ( ResultSet rs ) throws Exception {
			var subscriptionConfig = new SubscriptionConfig ();
			if ( rs.next () ) {
				subscriptionConfig.setId ( rs.getLong ( "id" ) );
				subscriptionConfig.setCodice ( rs.getString ( "codice" ) );
				subscriptionConfig.setUrl ( rs.getString ( "url" ) );
				subscriptionConfig.setDescrizione ( rs.getString ( "descrizione" ) );
				subscriptionConfig.setKeyPrimaria ( rs.getString ( "key_primaria" ) );
				subscriptionConfig.setKeySecondaria ( rs.getString ( "key_secondaria" ) );
				subscriptionConfig.setDataInizioValidita ( rs.getTimestamp ( "data_inizio_validita" ) );
				subscriptionConfig.setDataFineValidita ( rs.getTimestamp ( "data_fine_validita" ) );
				subscriptionConfig.setUtenteInserimento ( rs.getString ( "utente_inserimento" ) );
				subscriptionConfig.setUtenteModifica ( rs.getString ( "utente_modifica" ) );
				subscriptionConfig.setDataInserimento ( rs.getTimestamp ( "data_inserimento" ) );
				subscriptionConfig.setDataModifica ( rs.getTimestamp ( "data_modifica" ) );
			}
			return subscriptionConfig;
		}
	}

}
