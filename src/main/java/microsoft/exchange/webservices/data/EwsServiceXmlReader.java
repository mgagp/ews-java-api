/**************************************************************************
 * copyright file="EwsServiceXmlReader.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the EwsServiceXmlReader.java.
 **************************************************************************/

package microsoft.exchange.webservices.data;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * XML reader.
 * 
 */
class EwsServiceXmlReader extends EwsXmlReader {

	/** The service. */
	private ExchangeService service;

	/**
	 * Initializes a new instance of the EwsXmlReader class.
	 * 
	 * @param stream
	 *            the stream
	 * @param service
	 *            the service
	 * @throws Exception 
	 */
	protected EwsServiceXmlReader(InputStream stream, ExchangeService service)
			throws Exception {
		super(stream);
		this.service = service;
	}

	/**
	 * Converts the specified string into a DateTime objects.
	 * 
	 * @param dateTimeString
	 *            The date time string to convert.
	 * @return A DateTime representing the converted string.
	 */
	private Date convertStringToDateTime(String dateTimeString) {
		return this.service
				.convertUniversalDateTimeStringToDate(dateTimeString);		
	}
	
	/**
	 * Converts the specified string into a 
	 * unspecified Date object, ignoring offset.	
	 * @param dateTimeString
	 *            The date time string to convert.
	 * @return A DateTime representing the converted string.
	 * @throws java.text.ParseException
	 */	
    private Date convertStringToUnspecifiedDate(String dateTimeString) throws ParseException {
        return this.getService().
        convertStartDateToUnspecifiedDateTime(dateTimeString);
    }

	/**
	 * Reads the element value as date time.
	 * 
	 * @return Element value
	 * @throws Exception
	 *             the exception
	 */
	public Date readElementValueAsDateTime() throws Exception {
		return this.convertStringToDateTime(this.readElementValue());
	}

	/**
	 * Reads the element value as unspecified date.	 
	 * @return Element value	
	 * @throws Exception 
	 */	 
    public Date readElementValueAsUnspecifiedDate() throws Exception {
        return this.convertStringToUnspecifiedDate(this.readElementValue());
    }
    
	/**
	 * Reads the element value as date time, assuming it is unbiased (e.g.
	 * 2009/01/01T08:00) and scoped to service's time zone.
	 * 
	 * @return Date
	 * @throws Exception
	 *             the exception
	 */
	public Date readElementValueAsUnbiasedDateTimeScopedToServiceTimeZone()
			throws Exception {
		// Convert the element's value to a DateTime with no adjustment.
		String date = this.readElementValue();
		Date tempDate = null;

		try {
			DateFormat formatter = 
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			//formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			tempDate = formatter.parse(date);
		} catch (Exception e) {
			//e.printStackTrace();
			DateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSS");
			//formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			tempDate = formatter.parse(date);
		}

		/*
		 * TimeZone tz = sdfin.getTimeZone(); Calendar calen =
		 * Calendar.getInstance(); calen.setTime(tempDate);
		 */

		// Set the kind according to the service's time zone
		// Since the TimeZone is default UTC so no need to checks for Local and
		// other
		// if ((this.service.getTimeZone()).equals(TimeZone.getTimeZone("utc")))
		// {

		/*
		 * calen.setTimeInMillis(calen.getTimeInMillis() -
		 * tz.getOffset(tempDate.getTime()));
		 */
		return tempDate;
		/*
		 * } else if (EwsUtilities.isLocalTimeZone(this.service.getTimeZone()))
		 * { calen.setTimeInMillis(calen.getTimeInMillis() +
		 * tz.getOffset(tempDate.getTime())); return calen.getTime(); } else {
		 * return tempDate; }
		 */
	}

	/**
	 * Reads the element value as date time.
	 * 
	 * @param xmlNamespace
	 *            the xml namespace
	 * @param localName
	 *            the local name
	 * @return the date
	 * @throws Exception
	 *             the exception
	 */
	public Date readElementValueAsDateTime(XmlNamespace xmlNamespace,
			String localName) throws Exception {
		return this.convertStringToDateTime(this.readElementValue(xmlNamespace,
				localName));
	}

	/**
	 * Reads the service objects collection from XML.
	 * 
	 * @param <TServiceObject>
	 *            the generic type
	 * @param collectionXmlElementName
	 *            the collection xml element name
	 * @param getObjectInstanceDelegate
	 *            the get object instance delegate
	 * @param clearPropertyBag
	 *            the clear property bag
	 * @param requestedPropertySet
	 *            the requested property set
	 * @param summaryPropertiesOnly
	 *            the summary properties only
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings("unchecked")
	public <TServiceObject extends ServiceObject> List<TServiceObject> 
	readServiceObjectsCollectionFromXml(
			String collectionXmlElementName,
			IGetObjectInstanceDelegate<ServiceObject> 
			getObjectInstanceDelegate,
			boolean clearPropertyBag, PropertySet requestedPropertySet,
			boolean summaryPropertiesOnly) throws Exception {

		List<TServiceObject> serviceObjects = new ArrayList<TServiceObject>();
		TServiceObject serviceObject = null;

		this.readStartElement(XmlNamespace.Messages, collectionXmlElementName);

		if (!this.isEmptyElement()) {
			do {
				this.read();

				if (this.isStartElement()) {
					serviceObject = (TServiceObject)getObjectInstanceDelegate
							.getObjectInstanceDelegate(this.getService(), this
									.getLocalName());
					if (serviceObject == null) {
						this.skipCurrentElement();
					} else {
						if (!(this.getLocalName()).equals(serviceObject
								.getXmlElementName())) {

							throw new ServiceLocalException(String
									.format(
											"The type of the " + "object in " +
													 "the store (%s)" +
													 " does not match that" +
													 " of the " +
													 "local object (%s).",
											this.getLocalName(), serviceObject
													.getXmlElementName()));
						}
						serviceObject.loadFromXml(this, clearPropertyBag,
								requestedPropertySet, summaryPropertiesOnly);

						serviceObjects.add(serviceObject);
					}
				}
			} while (!this.isEndElement(XmlNamespace.Messages,
					collectionXmlElementName));
		} else {
			// For empty elements read End Element tag
			// i.e. position cursor on End Element
			this.read();
		}

		return serviceObjects;

	}

	/**
	 * Gets the service.
	 * 
	 * @return the service
	 */
	public ExchangeService getService() {
		return service;
	}

	/**
	 * Sets the service.
	 * 
	 * @param service
	 *            the new service
	 */
	public void setService(ExchangeService service) {
		this.service = service;
	}

}
