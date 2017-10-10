classdef KayakConnections < matlab.System & matlab.system.mixin.Propagates

    properties(Logical, Nontunable)
        debug = false;
    end

    properties
        TCPSourceName = 'APMPlanner';
        serialSourceName = 'Pixhawk'
        matlabSourceName = 'Matlab';
        dthostname = '127.0.0.1:3333';
        TCPSubscriptionHandle = 'PixhawkSource/PixhawkChannel';
        serialSubscriptionHandle = 'APMPlannerSource/APMPlannerChannel';
        serialPort = 'COM3';
        missionPlannerIP = '127.0.0.1'
        missionPlannerPort = 9001
    end

    properties(Access = private)
        baud = 57600;
        newData = false;
        data = [0; 0; 0; 0];
        mpConn;
        pixhawkConn;
        mDTConn;
    end

    methods (Static, Access = protected)
        % Add a tasteful and helpful blurb to the block header.
        function header = getHeaderImpl( )
            header = matlab.system.display.Header( mfilename( 'class' ), ...
            'Title', 'KayakConnections', ...
            'Text', ['A custom Simulink block for bidirectional communication' ...
            ' with APM/Mission Planner/QGroundControl and' ...
            ' a Pixhawk-esq autopilot using DataTurbine' ...
            ' that allows for Matlab to listen to the Pixhawk.' ...
            ' This program expects that a Pixhawk-esq thing' ...
            ' is connected over serial at the time this is' ...
            ' started. It also assumes that the Mission' ...
            ' Planner is setup as a TCP server listening on' ... 
            ' the port and IP.'] );
        end

        % Because it inlines Java code (for DataTurbine) this block can
        % only be simulated with Interpreted execution. Since the default
        % is code generation, we have to explicitly set this.
        function simMode = getSimulateUsingImpl( )
            simMode = 'Interpreted execution';
        end

        % Since we can only simulate in interpreted execution mode, why
        % even show the option to choose? I'm certain this is an entirely
        % future-proof decision that could never be bad or anything.
        function flag = showSimulateUsingImpl( )
            flag = false;
        end
    end

    methods
        % This is the default constructor, but because it is run when the
        % block is inserted into Simulink, not when the simulation is started,
        % we don't do anything here.
        function obj = decabot_source( varargin )
            setProperties( obj, nargin, varargin{:} );
        end
    end

    % Normally, on Simulink diagram execution, MATLAB performs a code generation
    % phase to automatically determine types and sizes of outputs. Because this
    % block interfaces with Java, it does not support code generation, which
    % causes this step to fail, unless we implement the following methods that
    % explicitly specify the sizes and types of the outputs. There is no need or
    % even way to specify the input sizes and types, as those are determined by
    % the outputs of the blocks feeding into the inputs.
    methods(Access = protected)
        function [sz1, sz2, sz3] = getOutputSizeImpl( ~ )
            sz1 = 1; sz2 = 1; sz3 = 1;
        end

        function [fz1, fz2, fz3] = isOutputFixedSizeImpl( ~ ) 
            fz1 = true; fz2 = true; fz3 = true;
        end

        function [dt1, dt2, dt3] = getOutputDataTypeImpl( ~ )
           dt1 = 'double'; dt2 = 'double'; dt3 = 'double'; 
        end

        function [cp1, cp2, cp3] = isOutputComplexImpl( ~ ) 
            cp1 = false; cp2 = false; cp3 = false;
        end

        function setupImpl( obj ) % TODO
            obj.mpConn = edu.scu.engr.rsl.connections.TCPToDT(obj.missionPlannerIP, obj.missionPlannerPort, obj.dthostname, obj.TCPSourceName, obj.TCPSubscriptionHandle)
            obj.pixhawkConn = edu.scu.engr.rsl.connections.SerialToDT(obj.serialPort, obj.baud, obj.dthostname, obj.serialSourceName, obj.serialSubscriptionHandle)
            obj.mDTConn = edu.scu.engr.rsl.connections.DTConnection(obj.dthostname, obj.matlabSourceName, obj.TCPSubscriptionHandle);
        end

        function [x, y, alt] = stepImpl( obj ) % TODO
        	bytesRead = obj.mDTConn.read();
            mavlinkParser = com.MAVLink.MAVLinkPacket(bytesRead.length);
			packet = mavlinkParser.unpack();
			if packet instanceof com.MAVLink.varsensor.msg_variable_sensor_field 
				% some sensor data, asking to be parsed
				%TODO
			elseif packet instanceof com.MAVLink.common.msg_global_position_int
				% some gps data
				time = packet.time_boot_ms;
				lat = packet.lat
				lon = packet.lon
				alt = packet.alt;
				relative_alt = packet.relative_alt;
				vx = packet.vx;
				vy = packet.vy;
				vz = packet.vz;
				heading = packet.heading;
			end
        end

        function releaseImpl(obj)
            obj.pixhawkConn.closeConnections();
            obj.mDTConn.closeConnections();
            obj.mDTConn.closeConnections();
        end
    end
end
