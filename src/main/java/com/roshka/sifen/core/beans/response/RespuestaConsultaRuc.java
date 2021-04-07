package com.roshka.sifen.core.beans.response;

import com.roshka.sifen.core.exceptions.SifenException;
import com.roshka.sifen.internal.response.SifenObjectFactory;
import com.roshka.sifen.internal.response.BaseResponse;
import com.roshka.sifen.core.fields.response.ruc.TxContRuc;
import org.w3c.dom.Node;

public class RespuestaConsultaRuc extends BaseResponse {
    private TxContRuc xContRUC;

    @Override
    public void setValueFromChildNode(Node value) throws SifenException {
        if (value.getLocalName().equals("xContRUC")) {
            xContRUC = SifenObjectFactory.getFromNode(value, TxContRuc.class);
        } else {
            super.setValueFromChildNode(value);
        }
    }

    public TxContRuc getxContRUC() {
        return xContRUC;
    }
}