package org.bitmarkets.bitnash;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.Transaction.SigHash;
import com.google.bitcoin.crypto.TransactionSignature;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.script.ScriptBuilder;

public class BNTxIn extends BNObject {
	public BNTxIn() {
		super();
		bnSlotNames.addAll(Arrays.asList(
				"scriptSig",
				"previousOutIndex",
				"previousTxSerializedHex",
				"previousTxHash"
		));
	}
	
	public BNScriptSig getScriptSig() {
		return scriptSig;
	}
	
	public void setScriptSig(BNScriptSig scriptSig) {
		this.scriptSig = scriptSig;
	}
	
	public Number getPreviousOutIndex() {
		return previousOutIndex;
	}
	
	public void setPreviousOutIndex(Number previousOutIndex) {
		this.previousOutIndex = previousOutIndex;
	}
	
	public String getPreviousTxSerializedHex() {
		return previousTxSerializedHex;
	}
	
	public void setPreviousTxSerializedHex(String previousTxSerializedHex) {
		this.previousTxSerializedHex = previousTxSerializedHex;
	}
	
	public String getPreviousTxHash() {
		return previousTxHash;
	}
	
	public void setPreviousTxHash(String previousTxHash) {
		this.previousTxHash = previousTxHash;
	}
	
	public int index() {
		return bnTx().getInputs().indexOf(this);
	}
	
	public void sign() {
		Script scriptPubKey = transactionInput().getConnectedOutput().getScriptPubKey();
		if (scriptPubKey.isSentToAddress()) {
			signPayToAddress();
		} else if (scriptPubKey.isSentToMultiSig()) {
			signMultisig();
		} else {
			throw new RuntimeException("Can't sign input w/ connected script: " + scriptPubKey.toString());
		}
	}
	
	BNScriptSig scriptSig;
	Number previousOutIndex;
	String previousTxSerializedHex;
	String previousTxHash;
	
	void signPayToAddress() {
		ECKey key = transactionInput().getOutpoint().getConnectedKey(wallet());
		TransactionSignature txSig = signUsingKey(key);
		if (txSig != null) {
			transactionInput().setScriptSig(ScriptBuilder.createInputScript(txSig, key));
		}
	}
	
	void signMultisig() {
		ArrayList<TransactionSignature> transactionSignatures = new ArrayList<TransactionSignature>();
		
		TransactionSignature existingTxSig = null;
		Script existingScriptSig = transactionInput().getScriptSig();
		
		if (existingScriptSig != null && existingScriptSig.getProgram().length > 0) {
			existingTxSig = new TransactionSignature(
					TransactionSignature.decodeFromDER(transactionInput().getScriptSig().getChunks().get(1).data),
					SigHash.ALL,
					false);
		}
		
		Script scriptPubKey = transactionInput().getConnectedOutput().getScriptPubKey();
		for (int i = 1; i <= 2; i ++) {
			TransactionSignature txSig = signUsingKey(wallet().findKeyFromPubKey(scriptPubKey.getChunks().get(i).data));
			if (txSig != null) {
				transactionSignatures.add(txSig);
			} else if (existingTxSig != null) {
				transactionSignatures.add(existingTxSig);
			}
		}
		
		transactionInput().setScriptSig(ScriptBuilder.createMultiSigInputScript(transactionSignatures));
	}
	
	TransactionSignature signUsingKey(ECKey key) {
		Transaction transaction = transaction();
		
		if (key != null) {
			return transaction.calculateSignature(
					index(),
					key,
					null,
					transactionInput().getOutpoint().getConnectedOutput().getScriptBytes(),
					SigHash.ALL,
					false
			);
		} else {
			return null;
		}
	}
	
	BNTx bnTx() {
		return (BNTx) getParent();
	}
	
	Transaction transaction() {
		return bnTx().getTransaction();
	}
	
	Wallet wallet() {
		return bnTx().wallet();
	}
	
	NetworkParameters networkParams() {
		return bnTx().networkParams();
	}
	
	TransactionInput transactionInput() {
		return transaction().getInput(index());
	}
	
	void didDeserializeSelf() {
		Transaction tx = transaction();
		
		Transaction previousTx = wallet().getTransaction(new Sha256Hash(previousTxHash));
		
		if (previousTx == null && previousTxSerializedHex != null) {
			previousTx = new Transaction(networkParams(), Utils.parseAsHexOrBase58(previousTxSerializedHex));
		}
		
		if (previousTx != null) {
			tx.addInput(previousTx.getOutput(previousOutIndex.intValue()));
		}
	}
	
	void willSerializeSelf() {
		setPreviousOutIndex(BigInteger.valueOf(transactionInput().getOutpoint().getIndex()));
		setPreviousTxHash(transactionInput().getConnectedOutput().getParentTransaction().getHashAsString());
		setPreviousTxSerializedHex(Utils.bytesToHexString(transactionInput().getConnectedOutput().getParentTransaction().bitcoinSerialize()));
		
		if (transactionInput().getScriptSig() != null && transactionInput().getScriptSig().getChunks().size() > 0) {
			scriptSig = new BNScriptSig();
			scriptSig.setBnParent(this);
			scriptSig.willSerialize();
		}
	}
}
