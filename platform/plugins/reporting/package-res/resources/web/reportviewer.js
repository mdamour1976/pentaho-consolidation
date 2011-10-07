var ReportViewer = {

  /**
   * Loads the parameter xml definition from the server.
   */
  fetchParameterDefinition: function() {
    var paramDefn = pentaho.common.prompting.paramDefn;
    var options = paramDefn ? paramDefn.getParameterValues() : {};
    options['renderMode'] = paramDefn ? 'PARAMETER' : 'XML';
    options['solution'] = Dashboards.getQueryParameter('solution');
    options['path'] = Dashboards.getQueryParameter('path');
    options['name'] = Dashboards.getQueryParameter('name');

    // Never send the session back. This is generated by the server.
    delete options['::session'];

    var newParamDefn;
    $.ajax({
      async: false,
      cache: false,
      type: 'POST',
      url: webAppPath + '/content/reporting',
      data: options,
      dataType:'text',
      success: function(xmlString){
        try {
          newParamDefn = pentaho.common.prompting.parseParameterDefinition(xmlString);
          // Make sure we retrain the current auto-submit setting
          var currentAutoSubmit = paramDefn ? paramDefn.getAutoSubmitSetting() : undefined;
          if (currentAutoSubmit != undefined) {
            newParamDefn.autoSubmitUI = currentAutoSubmit;
          }

          newParamDefn.createTextFormatter = ReportViewer.createTextFormatter.bind(ReportViewer);
        } catch (e) {
          alert('Error parsing parameter xml. ' + e); // TODO Replace with error dialog
        }
      },
      error: function(xml) {
        alert('error loading parameter information: ' + xml); // TODO replace with error dialog
      }
    });
    return newParamDefn;
  },

  /**
   * Create a text formatter that can convert between a parameter's defined format and the transport
   * format the Pentaho Reporting Engine expects.
   */
  createTextFormatter: function(parameter) {
    if (!createFormatter) {
      console.log("Unable to find formatter module. No text formatting will be possible.");
      return;
    }
    var dataFormat = parameter.attributes['data-format'];
    if (!parameter.list && dataFormat) {
      var formatter = createFormatter(parameter.type, parameter.attributes['data-format']);
      return this._createDataTransportFormatter(parameter, formatter);
    } else {
      return undefined;
    }
  },

  _formatTypeMap: {
    'number': 'number',
    'java.lang.Number': 'number',
    'java.lang.Byte': 'number',
    'java.lang.Short': 'number',
    'java.lang.Integer': 'number',
    'java.lang.Long': 'number',
    'java.lang.Float': 'number',
    'java.lang.Double': 'number',
    'java.math.BigDecimal': 'number',
    'java.math.BigInteger': 'number',
    
    'date': 'date',
    'java.util.Date': 'date',
    'java.sql.Date': 'date',
    'java.sql.Time': 'date',
    'java.sql.Timestamp': 'date'
  },

  _initDateFormatters: function() {
    // Lazily create all date formatters since we may not have createFormatter available when we're loaded
    if (!this.dateFormatters) {
      this.dateFormatters = {
        'with-timezone': createFormatter('date', "yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
        'without-timezone': createFormatter('date', "yyyy-MM-dd'T'HH:mm:ss.SSS"),
        'utc': createFormatter('date', "yyyy-MM-dd'T'HH:mm:ss.SSS'+0000'"),
        'simple': createFormatter('date', "yyyy-MM-dd")
      }
    }
  },

  /**
   * Create a formatter to pass data to/from the Pentaho Reporting Engine. This is to maintain compatibility
   * with the Parameter XML output from the Report Viewer.
   */
  _createDataTransportFormatter: function(parameter, formatter) {
    var formatterType = this._formatTypeMap[parameter.type];
    if (formatterType == 'number') {
      return {
        format: function(object) {
          return formatter.format(object);
        },
        parse: function(s) {
          return '' + formatter.parse(s);
        }
      }
    } else if (formatterType == 'date') {
      var transportFormatter = this._createDateTransportFormatter(parameter);
      return {
        format: function(s) {
          return formatter.format(transportFormatter.parse(s));
        },
        parse: function(s) {
          return transportFormatter.format(formatter.parse(s));
        }
      }
    }
  },

  /**
   * This text formatter converts a Date to/from the internal transport format (ISO-8601) used by Pentaho Reporting Engine
   * and found in parameter xml generated for Report Viewer.
   */
  _createDateTransportFormatter: function(parameter, s) {
    var timezone = parameter.attributes['timezone'];
    this._initDateFormatters();
    return {
      format: function(date) {
        if ('client' === timezone) {
          return this.dateFormatters['with-timezone'].format(date);
        }
        // Take the date string as it comes from the server, cut out the timezone information - the
        // server will supply its own here.
        if (parameter.timezoneHint) {
          if (!this.dateFormatters[parameter.timezoneHint]) {
            this.dateFormatters[parameter.timezoneHint] = createFormatter('date', "yyyy-MM-dd'T'HH:mm:ss.SSS" + "'" + parameter.timezoneHint + "'");
          }
          return this.dateFormatters[parameter.timezoneHint].format(date);
        } else {
          if ('server' === timezone || !timezone) {
            return this.dateFormatters['without-timezone'].format(date);
          } else if ('utc' === timezone) {
            return this.dateFormatters['utc'].format(date);
          } else {
            var offset = pentaho.common.prompting.timeutil.getOffsetAsString(timezone);
            if (!this.dateFormatters[offset]) {
              this.dateFormatters[offset] = createFormatter('date', "yyyy-MM-dd'T'HH:mm:ss.SSS'" + offset + "'");
            }
            return this.dateFormatters[offset].format(date);
          }
        }
      }.bind(this),
      parse: function(s) {
        if ('client' === timezone) {
          try {
            // Try to parse with timezone info
            return this.dateFormatters['with-timezone'].parse(s);
          } catch (e) {
            // ignore, keep trying
          }
        }
        try {
          // Try to parse without timezone info
          if (s.length === 28)
          {
            s = s.substring(0, 23);
          }
          return this.dateFormatters['without-timezone'].parse(s);
        } catch (e) {
          // ignore, keep trying
        }
        try {
          if (s.length == 10) {
            return this.dateFormatters['simple'].parse(s);
          }
        } catch (e) {
          // ignore, keep trying
        }
        try {
          return new Date(parseFloat(s));
        } catch (e) {
          // ignore, we're done here
        }
        return ''; // this represents a null in CDF
      }.bind(this)
    };
  },

  /**
   * Updates date values to make sure the timezone information is correct.
   */
  normalizeParameterValue: function(parameter, type, value) {
    if (value == null || type == null) {
      return null;
    }

    // Strip out actual type from Java array types
    var m = type.match('^\\[L([^;]+);$');
    if (m != null && m.length === 2) {
      type = m[1];
    }

    switch(type) {
      case 'java.util.Date':
      case 'java.sql.Date':
      case 'java.sql.Time':
      case 'java.sql.Timestamp':
        var timezone = parameter.attributes['timezone'];
        if (!timezone || timezone == 'server') {
          if (parameter.timezoneHint == undefined) {
            // Extract timezone hint from data if we can and update the parameter
            if (value.length == 28) {
              // Update the parameter's timezone hint
              parameter.timezoneHint = value.substring(23, 28);
            }
          }
          return value;
        }

        if(timezone == 'client') {
          return value;
        }

        // for every other mode (fixed timezone modes), translate the time into the specified timezone
        if ((parameter.timezoneHint != undefined && $.trim(parameter.timezoneHint).length != 0)
         && value.match(parameter.timezoneHint + '$'))
        {
          return value;
        }

        // the resulting time will have the same universal time as the original one, but the string
        // will match the timeoffset specified in the timezone.
        return this.convertTimeStampToTimeZone(value, timezone);
    }
    return value;
  },

  /**
   * Converts a time from a arbitary timezone into the local timezone. The timestamp value remains unchanged,
   * but the string representation changes to reflect the give timezone.
   *
   * @param value the timestamp as string in UTC format
   * @param timezone the target timezone
   * @return the converted timestamp string.
   */
  convertTimeStampToTimeZone: function(value, timezone) {
    // Lookup the offset in minutes
    var offset = pentaho.common.prompting.timeutils.getOffset(timezone);
    var localFormat = createFormatter('date', "yyyy-MM-dd'T'HH:mm:ss.SSS");

    var localDate = this.dateFormatters['without-timezone'].parse(value);
    var utcDate = this.dateFormatters['with-timezone'].parse(value);
    var offsetText = pentaho.common.prompting.timeutil.formatOffset(offset);

    var nativeOffset = -(new Date().getTimezoneOffset());

    var time = localDate.getTime() + (offset * 60000) + (utcDate.getTime() - localDate.getTime() - (nativeOffset * 60000));
    var localDateWithShift = new Date(time);

    return localFormat.format(localDateWithShift) + offsetText;
  }
};